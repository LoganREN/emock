package com.mzh.emock.type.bean.definition;

import com.mzh.emock.type.bean.method.EMockMethodInvoker;
import org.springframework.context.ApplicationContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.mzh.emock.type.bean.method.EMockMethodInvoker.*;

public class EMockBeanCreationMethodDefinition<T> {
    public EMockBeanCreationMethodDefinition(Method definitionMethod) {
        this.nativeMethod = definitionMethod;
        Annotation[] annotations=definitionMethod.getAnnotations();
        for(Annotation annotation:annotations){
            this.annotations.put(annotation.getClass(),annotation);
        }
    }
    private Class<T> targetClz;

    private final Method nativeMethod;
    private SimpleInvoker<EMockBeanInfoDefinition<T>, ApplicationContext> methodInvoker=new SimpleInvoker<EMockBeanInfoDefinition<T>, ApplicationContext>() {
        @Override
        public EMockBeanInfoDefinition<T> invoke(ApplicationContext args) throws InvocationTargetException, IllegalAccessException {
            return (EMockBeanInfoDefinition<T>) nativeMethod.invoke(null,args);
        }
    };
    private Map<Class<?>, Object> annotations=new ConcurrentHashMap<>();


    public Class<T> getTargetClz() {
        return targetClz;
    }

    public void setTargetClz(Class<T> targetClz) {
        this.targetClz = targetClz;
    }


    public SimpleInvoker<EMockBeanInfoDefinition<T>, ApplicationContext> getMethodInvoker() {
        return methodInvoker;
    }

    public void setMethodInvoker(SimpleInvoker<EMockBeanInfoDefinition<T>, ApplicationContext> methodInvoker) {
        this.methodInvoker = methodInvoker;
    }

    public Map<Class<?>, Object> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Map<Class<?>, Object> annotations) {
        this.annotations = annotations;
    }

}
