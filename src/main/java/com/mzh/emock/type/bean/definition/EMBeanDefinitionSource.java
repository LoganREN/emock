package com.mzh.emock.type.bean.definition;

import com.mzh.emock.type.EMBean;
import org.springframework.context.ApplicationContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.mzh.emock.type.bean.method.EMMethodInvoker.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

public class EMBeanDefinitionSource<T> {
    public EMBeanDefinitionSource(Method definitionMethod) {
        this.nativeMethod = definitionMethod;
        boolean is=definitionMethod.isAnnotationPresent(EMBean.class);
        Annotation[] annotations=definitionMethod.getAnnotations();
        for(Annotation annotation:annotations){
            this.annotations.put(annotation.annotationType(),annotation);
        }
    }
    private Class<T> targetClz;

    private final Method nativeMethod;
    private SimpleInvoker<EMBeanDefinition<T>, ApplicationContext> methodInvoker=new SimpleInvoker<EMBeanDefinition<T>, ApplicationContext>() {
        @Override
        public EMBeanDefinition<T> invoke(ApplicationContext args) throws InvocationTargetException, IllegalAccessException {
            return (EMBeanDefinition<T>) nativeMethod.invoke(null,args);
        }
    };
    private Map<Class<?>, Object> annotations=new ConcurrentHashMap<>();


    public Class<T> getTargetClz() {
        return targetClz;
    }

    public void setTargetClz(Class<T> targetClz) {
        this.targetClz = targetClz;
    }


    public SimpleInvoker<EMBeanDefinition<T>, ApplicationContext> getMethodInvoker() {
        return methodInvoker;
    }

    public void setMethodInvoker(SimpleInvoker<EMBeanDefinition<T>, ApplicationContext> methodInvoker) {
        this.methodInvoker = methodInvoker;
    }

    public Map<Class<?>, Object> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Map<Class<?>, Object> annotations) {
        this.annotations = annotations;
    }

    public int getOrder(){
        Order order = (Order) this.annotations.get(Order.class);
        if (order != null) {
            return order.value();
        }
        EMBean orderN = (EMBean) this.annotations.get(EMBean.class);
        if (orderN != null) {
            return orderN.order();
        }
        return Ordered.LOWEST_PRECEDENCE;
    }

}
