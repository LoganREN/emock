package com.mzh.emock.type.bean.definition;

import com.mzh.emock.type.bean.EMockBeanWrapper;
import org.springframework.lang.NonNull;

public class EMockBeanInfoDefinition<T> {
    private Class<T> classMatcher;
    private String nameMatcher;
    private EMockBeanWrapper<T> wrapper;


    public EMockBeanInfoDefinition(Class<T> mockedClass, EMockBeanWrapper<T> wrapper){
        initial(mockedClass,null,wrapper);
    }
    public EMockBeanInfoDefinition(Class<T> classMatcher, @NonNull String nameMatcher, EMockBeanWrapper<T> beanWrapper){
        initial(classMatcher,nameMatcher,beanWrapper);
    }
    private void initial(Class<T> mockedClass,String nameMatcher,EMockBeanWrapper<T> beanWrapper){
        this.nameMatcher=nameMatcher;
        this.classMatcher=mockedClass;
        this.wrapper=beanWrapper;
    }

    public Class<T> getClassMatcher() {
        return classMatcher;
    }

    public void setClassMatcher(Class<T> classMatcher) {
        this.classMatcher = classMatcher;
    }

    public String getNameMatcher() {
        return nameMatcher;
    }

    public void setNameMatcher(String nameMatcher) {
        this.nameMatcher = nameMatcher;
    }


    public EMockBeanWrapper<T> getWrapper() {
        return wrapper;
    }

    public void setWrapper(EMockBeanWrapper<T> wrapper) {
        this.wrapper = wrapper;
    }
}
