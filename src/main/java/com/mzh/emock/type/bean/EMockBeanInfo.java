package com.mzh.emock.type.bean;

import com.mzh.emock.type.bean.definition.EMockBeanCreationMethodDefinition;
import com.mzh.emock.type.bean.definition.EMockBeanInfoDefinition;
import com.mzh.emock.type.bean.method.EMockMethodInfo;
import com.mzh.emock.util.EMockUtil;
import org.springframework.lang.NonNull;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EMockBeanInfo<T>{

    private boolean isMocked;
    private T mockedBean;
    private EMockBeanInfoDefinition<T> beanInfoDefinition;
    private EMockBeanCreationMethodDefinition<T> creationMethodDefinition;
    private Map<String, EMockMethodInfo> invokeMethods=new ConcurrentHashMap<>();

    public EMockBeanInfo(@NonNull T mockedBean,
                         @NonNull EMockBeanInfoDefinition<T> beanInfoDefinition,
                         @NonNull EMockBeanCreationMethodDefinition<T> creationMethodDefinition){
        this.mockedBean=mockedBean;
        this.beanInfoDefinition=beanInfoDefinition;
        this.creationMethodDefinition=creationMethodDefinition;
        EMockUtil.optWithParent(beanInfoDefinition.getClassMatcher(), c->{
            if(c!=Object.class) {
                Method[] methods = c.getDeclaredMethods();
                for(Method method:methods) {
                    EMockMethodInfo methodInfo=new EMockMethodInfo();
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

    public EMockBeanInfoDefinition<T> getBeanInfoDefinition() {
        return beanInfoDefinition;
    }

    public void setBeanInfoDefinition(EMockBeanInfoDefinition<T> beanInfoDefinition) {
        this.beanInfoDefinition = beanInfoDefinition;
    }

    public EMockBeanCreationMethodDefinition<T> getCreationMethodDefinition() {
        return creationMethodDefinition;
    }

    public void setCreationMethodDefinition(EMockBeanCreationMethodDefinition<T> creationMethodDefinition) {
        this.creationMethodDefinition = creationMethodDefinition;
    }

    public Map<String, EMockMethodInfo> getInvokeMethods() {
        return invokeMethods;
    }

    public void setInvokeMethods(Map<String, EMockMethodInfo> invokeMethods) {
        this.invokeMethods = invokeMethods;
    }
}
