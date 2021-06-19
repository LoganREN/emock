package com.mzh.emock.util;

import com.mzh.emock.core.EMCache;
import com.mzh.emock.type.proxy.EMProxyHolder;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class EMProxyTool {

    public static Object createProxy(Class<?> targetClz, Object oldBean) {
        Object cached=findCreatedProxy(targetClz,oldBean);
        if(cached!=null){
            return cached;
        }
        ClassLoader loader= EMProxyTool.class.getClassLoader();
        Object proxy=(targetClz.isInterface() ? createInterfaceProxy(new Class<?>[]{targetClz}, oldBean,loader) : createClassProxy(oldBean,loader));
        EMCache.EM_CACHED_PROXY.add(new EMProxyHolder(oldBean,targetClz,proxy));
        return proxy;
    }

    private static Object createInterfaceProxy(Class<?>[] interfaces, Object oldBean,ClassLoader loader) {
        if (oldBean instanceof Proxy) {
            InvocationHandler oldHandler = Proxy.getInvocationHandler(oldBean);
            return Proxy.newProxyInstance(loader, interfaces,
                    createEnhance(oldHandler, new EMCache.EProxyHandlerEnhanceInterceptor(oldHandler, oldBean),loader));
        }
        return Proxy.newProxyInstance(loader, interfaces, new EMCache.EInterfaceProxyInvocationHandler(oldBean));
    }


    private static Object createClassProxy(Object oldBean,ClassLoader loader) {
        return createEnhance(oldBean, new EMCache.EObjectEnhanceInterceptor(oldBean),loader);
    }

    private static Object findCreatedProxy(Class<?> targetClz,Object oldBean){
        for(EMProxyHolder holder:EMCache.EM_CACHED_PROXY){
            if(holder.matched(targetClz,oldBean)){
                return holder.getProxy();
            }
        }
        return null;
    }

    private static <T> T createEnhance(T old, MethodInterceptor methodInterceptor, ClassLoader loader) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(old.getClass());
        enhancer.setClassLoader(loader);
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
}