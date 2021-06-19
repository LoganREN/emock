package com.mzh.emock.type.bean;

import com.mzh.emock.type.bean.definition.EMBeanDefinitionSource;
import com.mzh.emock.type.bean.definition.EMBeanDefinition;
import com.mzh.emock.type.bean.method.EMMethodInfo;
import com.mzh.emock.util.EMUtil;
import org.springframework.lang.NonNull;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EMBeanInfo<T>{

    private boolean isMocked;
    private T mockedBean;
    private EMBeanDefinition<T> emBeanDefinition;
    private EMBeanDefinitionSource<T> emBeanDefinitionSource;
    private Map<String, EMMethodInfo> invokeMethods=new ConcurrentHashMap<>();

    public EMBeanInfo(@NonNull T mockedBean,
                      @NonNull EMBeanDefinition<T> emBeanDefinition,
                      @NonNull EMBeanDefinitionSource<T> emBeanDefinitionSource){
        this.mockedBean=mockedBean;
        this.emBeanDefinition=emBeanDefinition;
        this.emBeanDefinitionSource=emBeanDefinitionSource;
        EMUtil.optWithParent(emBeanDefinition.getClassMatcher(), c->{
            if(c!=Object.class) {
                Method[] methods = c.getDeclaredMethods();
                for(Method method:methods) {
                    EMMethodInfo methodInfo=new EMMethodInfo();
                    methodInfo.setName(method.getName());
                    methodInfo.setNativeMethod(method);
                    this.invokeMethods.put(method.getName(),methodInfo);
                }
            }
        });
    }

    public boolean isMocked() {
        return isMocked;
    }

    public void setMocked(boolean mocked) {
        isMocked = mocked;
    }

    public T getMockedBean() {
        return mockedBean;
    }

    public void setMockedBean(T mockedBean) {
        this.mockedBean = mockedBean;
    }

    public EMBeanDefinition<T> getEmBeanDefinition() {
        return emBeanDefinition;
    }

    public void setEmBeanDefinition(EMBeanDefinition<T> emBeanDefinition) {
        this.emBeanDefinition = emBeanDefinition;
    }

    public EMBeanDefinitionSource<T> getEmBeanDefinitionSource() {
        return emBeanDefinitionSource;
    }

    public void setEmBeanDefinitionSource(EMBeanDefinitionSource<T> emBeanDefinitionSource) {
        this.emBeanDefinitionSource = emBeanDefinitionSource;
    }

    public Map<String, EMMethodInfo> getInvokeMethods() {
        return invokeMethods;
    }

    public void setInvokeMethods(Map<String, EMMethodInfo> invokeMethods) {
        this.invokeMethods = invokeMethods;
    }
}
