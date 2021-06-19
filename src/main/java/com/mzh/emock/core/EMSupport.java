package com.mzh.emock.core;

import com.mzh.emock.EMConfigurationProperties;
import com.mzh.emock.log.Logger;
import com.mzh.emock.type.EMBean;
import com.mzh.emock.type.bean.EMBeanInfo;
import com.mzh.emock.type.bean.definition.EMBeanDefinitionSource;
import com.mzh.emock.type.bean.definition.EMBeanDefinition;
import com.mzh.emock.util.EMObjectMatcher;
import com.mzh.emock.util.EMProxyTool;
import com.mzh.emock.util.EMUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.PatternMatchUtils;

import java.lang.reflect.*;
import java.util.*;

public class EMSupport {
    private static final Logger logger = Logger.get(EMSupport.class);

    public static void loadEMDefinitionSource(ApplicationContext context, ResourceLoader loader) throws Exception {
        if (context == null || loader == null) {
            return;
        }
        EMCache.EM_DEFINITION_SOURCES.clear();
        String[] matchers = loadEMNameMatcher(context);
        List<Method> methods = loadEMDefinitionSources(loader);
        methods.stream().filter(m->PatternMatchUtils.simpleMatch(matchers,
                ((ParameterizedType) m.getGenericReturnType()).getActualTypeArguments()[0].getTypeName()))
                .forEach(m->EMCache.EM_DEFINITION_SOURCES.add(new EMBeanDefinitionSource<>(m)));
    }

    public static void createEMDefinition(ApplicationContext context) throws Exception {
        EMCache.EM_DEFINITIONS.clear();
        for (EMBeanDefinitionSource<?> ds : EMCache.EM_DEFINITION_SOURCES) {
            EMBeanDefinition<?> beanInfoDefinition = ds.getMethodInvoker().invoke(context);
            EMCache.EM_DEFINITION_RELATION.put(beanInfoDefinition, ds);
            EMCache.EM_DEFINITIONS.add(beanInfoDefinition);
        }
    }

    public static void createEMBeanIfNecessary(ApplicationContext context) {
        Arrays.stream(context.getBeanDefinitionNames()).forEach(name-> createEMBeanIfNecessary(name, context.getBean(name)));
    }

    /**
     * 实际mock过程,检查所有的bean是否需要进行mock
     *
     * @param beanName
     * @param oldBean
     */
    public static void createEMBeanIfNecessary(String beanName, Object oldBean) {
        EMCache.EM_DEFINITIONS.stream().filter(d->d.isMatch(beanName,oldBean)).forEach(d->{
            EMCache.EM_OBJECT_MAP.computeIfAbsent(oldBean,l->new ArrayList<>());
            EMBeanInfo<?> newBean=createMockBean(oldBean,d);
            EMCache.EM_OBJECT_MAP.get(oldBean).add(newBean);
            EMCache.EM_OBJECT_MAP.values().forEach(l->l.sort(Comparator.comparingInt(a -> a.getEmBeanDefinitionSource().getOrder())));
        });
    }

    private static <T> EMBeanInfo<T> createMockBean(Object oldBean, EMBeanDefinition<T> emBeanDefinition) {
        T nb = emBeanDefinition.getWrapper().wrap(emBeanDefinition.getClassMatcher().cast(oldBean));
        EMBeanDefinitionSource<T> ds= (EMBeanDefinitionSource<T>) EMCache.EM_DEFINITION_RELATION.get(emBeanDefinition);
        EMBeanInfo<T> i=new EMBeanInfo<>(nb, emBeanDefinition,ds);
        i.setMocked(true);
        return i;
    }

    /**
     * 将mock的bean注入到具体的实例中
     */
    public static void proxyAndInject(ApplicationContext context) throws Exception {
        String[] names = context.getBeanDefinitionNames();
        Object[] beans= Arrays.stream(names).map(context::getBean).toArray();
        for (Object target : EMCache.EM_OBJECT_MAP.keySet()) {
            for (int j=0;j<names.length;j++) {
                createProxyAndSetField(beans[j], target);
            }
        }
    }

    private static void createProxyAndSetField(Object src, Object target) throws Exception {
        Class<?> defaultClz=EMCache.EM_OBJECT_MAP.get(target).get(0).getEmBeanDefinition().getClassMatcher();
        Map<Object, List<EMObjectMatcher.FieldInfo>> matchedObject = EMObjectMatcher.match(src, target);
        for (Object holder : matchedObject.keySet()) {
            List<EMObjectMatcher.FieldInfo> fields = matchedObject.get(holder);
            for(int i=fields.size()-1;i>=0;i--){
                createProxyAndSetField(fields.get(i),holder,target,defaultClz);
            }
        }
    }
    private static void createProxyAndSetField(EMObjectMatcher.FieldInfo info, Object holder,Object target,Class<?> defaultClz) throws Exception {
        if(info.isArrayIndex()){
            Object old=((Object[])holder)[info.getIndex()];
            if(old==target) {
                Object proxy = EMProxyTool.createProxy(defaultClz, target);
                ((Object[]) holder)[info.getIndex()] = proxy;
            }else{
                logger.error("array object index changed "+",obj:"+holder);
            }
        }else{
            Class<?> fieldClz=info.getNativeField().getType();
            if(fieldClz.isAssignableFrom(defaultClz)){
                fieldClz=defaultClz;
            }
            Object proxy= EMProxyTool.createProxy(fieldClz, target);
            info.getNativeField().setAccessible(true);
            info.getNativeField().set(holder,proxy);
        }
    }


    private static String[] loadEMNameMatcher(ApplicationContext context) {
        Environment environment = context.getEnvironment();
        if (!isEMEnvironment(environment)) {
            return new String[]{};
        }
        List<String> filters=EMConfigurationProperties.FILTER;
        return filters.size() == 0 ? new String[]{} : filters.toArray(new String[0]);
    }

    private static boolean isEMEnvironment(Environment environment) {
        String[] envProfiles = environment.getActiveProfiles();
        List<String> targetProfiles = EMConfigurationProperties.ENABLED_PROFILES;
        if (envProfiles.length == 0 || targetProfiles.size() == 0) {
            return false;
        }
        for (String envProfile : envProfiles) {
            if (targetProfiles.contains(envProfile)) {
                return true;
            }
        }
        return false;
    }

    private static List<Method> loadEMDefinitionSources(ResourceLoader resourceLoader) throws Exception {
        ClassLoader classLoader = EMSupport.class.getClassLoader();
        List<Method> methods = new ArrayList<>();
        ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
        MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resourceLoader);
        List<String> paths = EMConfigurationProperties.SCAN_PATH;
        for (String path : paths) {
            Resource[] resources = resolver.getResources(EMUtil.formatResourcePath(path));
            for (Resource resource : resources) {
                MetadataReader reader = readerFactory.getMetadataReader(resource);
                Class<?> clz = classLoader.loadClass(reader.getClassMetadata().getClassName());
                EMUtil.optWithParent(clz, c -> {
                    Method[] clzMethods = c.getDeclaredMethods();
                    for (Method method : clzMethods) {
                        if (isEMDefinition(method)) {
                            methods.add(method);
                        }
                    }
                });
            }
        }
        return methods;
    }

    private static boolean isEMDefinition(Method method) {
        return (method.getModifiers() & Modifier.PUBLIC) > 0
                && (method.getModifiers() & Modifier.STATIC) > 0
                && method.isAnnotationPresent(EMBean.class)
                && method.getParameterCount() == 1
                && ApplicationContext.class.isAssignableFrom(method.getParameterTypes()[0])
                && method.getReturnType() == EMBeanDefinition.class
                && ParameterizedType.class.isAssignableFrom(method.getGenericReturnType().getClass())
                && ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments() != null
                && ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments().length == 1;
    }

}