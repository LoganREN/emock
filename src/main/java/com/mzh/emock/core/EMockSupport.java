package com.mzh.emock.core;

import com.mzh.emock.log.LogSupport;
import com.mzh.emock.type.bean.EMockBeanInfo;
import com.mzh.emock.type.bean.definition.EMockBeanCreationMethodDefinition;
import com.mzh.emock.type.bean.definition.EMockBeanInfoDefinition;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
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

public class EMockSupport {
    private static final LogSupport logger = LogSupport.getLogger(EMockSupport.class);

    public static void loadMatchedBeanDefinition(ApplicationContext context, ResourceLoader resourceLoader) throws Exception {
        EMockCache.mockBeanCreationMethodDefinitions.clear();
        if (context == null || resourceLoader == null) {
            return;
        }
        String[] matchers = loadMockBeanNameMatcher(context);
        List<Method> methods = loadBeanDefinitionMethod(resourceLoader);
        for (Method method : methods) {
            Type type = ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
            if (PatternMatchUtils.simpleMatch(matchers, type.getTypeName())) {
                EMockCache.mockBeanCreationMethodDefinitions.add(new EMockBeanCreationMethodDefinition<>(method));
            }
        }
    }

    public static void createMockBeanDefinition(ApplicationContext context) throws Exception {
        EMockCache.mockBeanInfoDefinitions.clear();
        for (EMockBeanCreationMethodDefinition<?> methodDefinition : EMockCache.mockBeanCreationMethodDefinitions) {
            EMockBeanInfoDefinition<?> beanInfoDefinition=methodDefinition.getMethodInvoker().invoke(context);
            EMockCache.definitionRelation.put(beanInfoDefinition,methodDefinition);
            EMockCache.mockBeanInfoDefinitions.add(beanInfoDefinition);
        }
    }

    public static void createMockBeanIfNecessary(ApplicationContext context) {
        String[] names = context.getBeanDefinitionNames();
        for (String name : names) {
            createMockBeanIfNecessary(name, context.getBean(name));
        }
    }


    /**
     * 实际mock过程,检查所有的bean是否需要进行mock
     *
     * @param beanName
     * @param oldBean
     */
    public static void createMockBeanIfNecessary(String beanName, Object oldBean) {
        for(EMockBeanInfoDefinition<?> def:EMockCache.mockBeanInfoDefinitions){
            if (isMatchMock(beanName, oldBean, def)) {
                if (EMockCache.mockObjectMap.get(oldBean) == null) {
                    EMockCache.mockObjectMap.computeIfAbsent(oldBean,k->new ArrayList<>());
                }
                EMockBeanInfo<?> mockBeanInfo = createMockBean(oldBean,def,EMockCache.definitionRelation.get(def));
                EMockCache.mockObjectMap.get(oldBean).add(mockBeanInfo);
            }
        }
    }

    public static void afterCreateMockBean() {
        for (PMockBeanInfoHolder<?> holder : PMockCache.mapObjectMap.values()) {
            holder.getMockedBeanList().sort(Comparator.comparingInt(a -> getOrder(a.getHolder().getMethodDefinition())));
        }
    }

    private static int getOrder(PMockMethodDefinition<?> definition) {
        Order order = (Order) definition.getAnnotations().get(Order.class);
        if (order != null) {
            return order.value();
        }
        PMockBean orderN = (PMockBean) definition.getAnnotations().get(PMockBean.class);
        if (orderN != null) {
            return orderN.order();
        }
        return Ordered.LOWEST_PRECEDENCE;
    }

    private static boolean isMatchMock(String beanName, Object oldBean, EMockBeanInfoDefinition<?> definition) {
        return (definition.getNameMatcher() == null
                || PatternMatchUtils.simpleMatch(definition.getNameMatcher(), beanName))
                && definition.getClassMatcher().isAssignableFrom(oldBean.getClass());
    }

    private static <T> EMockBeanInfo<T> createMockBean(T oldBean,
                                                       EMockBeanInfoDefinition<T> beanDefinition,
                                                       EMockBeanCreationMethodDefinition<T> methodDefinition) {
        T newBean = beanDefinition.getWrapper().wrap(oldBean);
        return new EMockBeanInfo<>(newBean, beanDefinition,methodDefinition);
    }

    /**
     * 将mock的bean注入到具体的实例中
     */
    public static void proxyAndInject(ApplicationContext context) throws Exception {
        String[] names = context.getBeanDefinitionNames();
        for (Object target : PMockCache.mapObjectMap.keySet()) {
            MatchedObject.reset();
            for (String name : names) {
                createProxyAndSetField(context.getBean(name), target);
            }
        }
    }

    private static void createProxyAndSetField(Object src, Object target) throws Exception {
        Class<?> defaultClz=PMockCache.mapObjectMap.get(target).getMockedBeanList().get(0).getHolder().getBeanDefinition().getClassMatcher();
        MatchedObject matchedObject = MatchedObject.match(src, target);
        Map<Object,List<MatchedObject.FieldDescription>> holder = matchedObject.getHoldingObject();
        for (Object ho : holder.keySet()) {
            List<MatchedObject.FieldDescription> fields = holder.get(ho);
            for(int i=fields.size()-1;i>=0;i--){
                MatchedObject.FieldDescription description=fields.get(i);
                if(description.isArrayIndex()){
                    Object old=((Object[])ho)[description.getIndex()];
                    if(old==target) {
                        Object proxy = createProxy(defaultClz, target);
                        ((Object[]) ho)[description.getIndex()] = proxy;
                    }else{
                        logger.error("array object index changed i:"+i+",obj:"+ho);
                    }
                }else{
                    Class<?> fieldClz=description.getNativeField().getType();
                    if(fieldClz.isAssignableFrom(defaultClz)){
                        fieldClz=defaultClz;
                    }
                    Object proxy=createProxy(fieldClz, target);
                    description.getNativeField().setAccessible(true);
                    description.getNativeField().set(ho,proxy);
                }
            }
        }
    }


    private static Object createProxy(Class<?> targetClz, Object oldBean) {
        Object cached=findCreatedProxy(targetClz,oldBean);
        if(cached!=null){
            return cached;
        }
        Object proxy=(targetClz.isInterface() ? createInterfaceProxy(new Class<?>[]{targetClz}, oldBean) : createClassProxy(oldBean));
        PMockCache.cachedProxy.computeIfAbsent(oldBean,o->new ArrayList<>());
        PMockCache.cachedProxy.get(oldBean).add(new ProxyDescription(targetClz,proxy));
        return proxy;
    }

    private static Object createInterfaceProxy(Class<?>[] interfaces, Object oldBean) {
        if (oldBean instanceof Proxy) {
            InvocationHandler oldHandler = Proxy.getInvocationHandler(oldBean);
            return Proxy.newProxyInstance(PMockSupport.class.getClassLoader(),
                    interfaces, createEnhance(oldHandler, new PMockCache.PHandlerEnhanceInvocationHandler(oldHandler, oldBean)));
        }
        return Proxy.newProxyInstance(PMockSupport.class.getClassLoader(), interfaces, new PMockCache.PInterfaceProxyInvocationHandler(oldBean));
    }


    private static Object createClassProxy(Object oldBean) {
        return createEnhance(oldBean, new PMockCache.PObjectEnhanceInvocationHandler(oldBean));
    }
    private static Object findCreatedProxy(Class<?> target,Object oldBean){
        return null;
        /*
        List<ProxyDescription> descriptions=PMockCache.cachedProxy.get(oldBean);
        if(descriptions==null){
            return null;
        }
        for(ProxyDescription description:descriptions){
            if(description.match(target)){
                return description.getProxy();
            }
        }
        return null;

         */
    }


    private static <T> T createEnhance(T old, MethodInterceptor methodInterceptor) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(old.getClass());
        enhancer.setUseCache(false);
        enhancer.setCallback(methodInterceptor);
        Constructor<?>[] cons = old.getClass().getDeclaredConstructors();
        Constructor<?> usedCon = null;
        for (Constructor<?> con : cons) {
            if (usedCon == null) {
                usedCon = con;
                continue;
            }
            if (con.getParameterCount() < usedCon.getParameterCount()) {
                usedCon = con;
            }
        }
        Object proxy;
        assert usedCon != null;
        if (usedCon.getParameterCount() == 0) {
            proxy = enhancer.create();
        } else {
            Object[] args = new Object[usedCon.getParameterCount()];
            proxy = enhancer.create(usedCon.getParameterTypes(), args);
        }
        return (T) proxy;
    }

    private static String[] loadMockBeanNameMatcher(ApplicationContext context) {
        Environment environment = context.getEnvironment();
        if (!isEnvironmentEnabledMock(environment)) {
            return new String[]{};
        }
        String propertyKey = PMockConfigurationProperties.PREFIX + "." + PMockConfigurationProperties.NAME;
        String value = PMockUtil.removeSpace(environment.getProperty(propertyKey));
        return value.length() == 0 ? new String[]{} : value.split(",");
    }

    private static boolean isEnvironmentEnabledMock(Environment environment) {
        String[] envProfiles = environment.getActiveProfiles();
        List<String> targetProfiles = PMockConfigurationProperties.enabledProfiles;
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

    private static List<Method> loadBeanDefinitionMethod(ResourceLoader resourceLoader) throws Exception {
        ClassLoader classLoader = PMockSupport.class.getClassLoader();
        List<Method> methods = new ArrayList<>();
        ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
        MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resourceLoader);
        List<String> paths = PMockConfigurationProperties.scanPath;
        for (String path : paths) {
            Resource[] resources = resolver.getResources(PMockUtil.formatResourcePath(path));
            for (Resource resource : resources) {
                MetadataReader reader = readerFactory.getMetadataReader(resource);
                Class<?> clz = classLoader.loadClass(reader.getClassMetadata().getClassName());
                PMockUtil.hierarchyClz(clz, c -> {
                    Method[] clzMethods = c.getDeclaredMethods();
                    for (Method method : clzMethods) {
                        if (isMockBeanDefinitionMethod(method)) {
                            methods.add(method);
                        }
                    }
                });
            }
        }
        return methods;
    }

    private static boolean isMockBeanDefinitionMethod(Method method) {
        return (method.getModifiers() & Modifier.PUBLIC) > 0
                && (method.getModifiers() & Modifier.STATIC) > 0
                && method.isAnnotationPresent(PMockBean.class)
                && method.getParameterCount() == 1
                && ApplicationContext.class.isAssignableFrom(method.getParameterTypes()[0])
                && method.getReturnType() == PMockBeanDefinition.class
                && ParameterizedType.class.isAssignableFrom(method.getGenericReturnType().getClass())
                && ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments() != null
                && ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments().length == 1;
    }

}