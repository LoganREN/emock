package com.mzh.emock.core;

import com.mzh.emock.type.bean.EMBeanInfo;
import com.mzh.emock.type.bean.definition.EMBeanDefinitionSource;
import com.mzh.emock.type.bean.definition.EMBeanDefinition;
import com.mzh.emock.type.bean.method.EMMethodInfo;
import com.mzh.emock.type.bean.method.EMMethodInvoker;
import com.mzh.emock.type.bean.method.EMMethodInvoker.*;
import com.mzh.emock.type.proxy.EMProxyHolder;
import com.mzh.emock.util.EMObjectMap;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EMCache {

    public static final List<EMBeanDefinitionSource<?>> EM_DEFINITION_SOURCES = new ArrayList<>();

    public static final List<EMBeanDefinition<?>> EM_DEFINITIONS = new ArrayList<>();

    public static final Map<EMBeanDefinition<?>, EMBeanDefinitionSource<?>> EM_DEFINITION_RELATION=new EMObjectMap<>();

    public static final Map<Object, List<EMBeanInfo<?>>> EM_OBJECT_MAP = new EMObjectMap<>();

    public static final List<EMProxyHolder> EM_CACHED_PROXY = new ArrayList<>();

    static class ESimpleInvoker implements SimpleInvoker<Object, Object[]> {
        private final Object bean;
        private final Method method;

        ESimpleInvoker(Object bean, Method method) {
            this.bean = bean;
            this.method = method;
        }

        @Override
        public Object invoke(Object[] args) throws InvocationTargetException, IllegalAccessException {
            return method.invoke(bean, args);
        }
    }

    private static Object doMock(Object o, Method method, Object[] args, Object oldBean) throws Exception {
        List<EMBeanInfo<?>> mockBeanInfoList = EM_OBJECT_MAP.get(oldBean);
        if (mockBeanInfoList == null || mockBeanInfoList.size()==0) {
            return null;
        }
        for(EMBeanInfo<?> mockBeanInfo:mockBeanInfoList){
            if(mockBeanInfo.isMocked()){
                Map<String, EMMethodInfo> invokeMethods = mockBeanInfo.getInvokeMethods();
                EMMethodInfo methodInfo = invokeMethods.get(method.getName());
                if (methodInfo.getEnabledInvoker() != null) {
                    EMMethodInvoker<Object, Object[]> dynamicInvoker = methodInfo.getDynamicInvokers().get(methodInfo.getEnabledInvoker());
                    return dynamicInvoker.invoke(new ESimpleInvoker(oldBean, method), new ESimpleInvoker(mockBeanInfo.getMockedBean(), method), args);
                }
                return method.invoke(mockBeanInfo.getMockedBean(), args);
            }
        }
        return null;
    }

    public static class EInterfaceProxyInvocationHandler implements InvocationHandler {
        private final Object oldBean;

        public EInterfaceProxyInvocationHandler(Object oldBean) {
            this.oldBean = oldBean;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object mock = doMock(proxy, method, args, oldBean);
            return mock == null ? method.invoke(oldBean, args) : mock;
        }
    }

    public static class EObjectEnhanceInterceptor implements MethodInterceptor {
        private final Object oldBean;

        public EObjectEnhanceInterceptor(Object oldBean) {
            this.oldBean = oldBean;
        }

        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            Object mock = doMock(o, method, objects, oldBean);
            return mock == null ? method.invoke(oldBean, objects) : mock;
        }
    }

    public static class EProxyHandlerEnhanceInterceptor implements MethodInterceptor {
        private final InvocationHandler oldHandler;
        private final Object oldBean;

        public EProxyHandlerEnhanceInterceptor(InvocationHandler oldHandler, Object oldBean) {
            this.oldBean = oldBean;
            this.oldHandler = oldHandler;
        }

        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            Method realMethod = (Method) objects[1];
            Object mock = doMock(o, realMethod, (Object[]) objects[2], oldBean);
            return mock == null ? oldHandler.invoke(oldBean, realMethod, (Object[]) objects[2]) : mock;
        }
    }

}
