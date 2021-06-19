package com.mzh.emock.type.bean.method;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class EMMethodInfo {
    private String name;
    private Map<String, EMMethodInvoker<Object,Object[]>> dynamicInvokers=new HashMap<>();
    private Method nativeMethod;
    private String enabledInvoker;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, EMMethodInvoker<Object, Object[]>> getDynamicInvokers() {
        return dynamicInvokers;
    }

    public void setDynamicInvokers(Map<String, EMMethodInvoker<Object, Object[]>> dynamicInvokers) {
        this.dynamicInvokers = dynamicInvokers;
    }

    public Method getNativeMethod() {
        return nativeMethod;
    }

    public void setNativeMethod(Method nativeMethod) {
        this.nativeMethod = nativeMethod;
    }

    public String getEnabledInvoker() {
        return enabledInvoker;
    }

    public void setEnabledInvoker(String enabledInvoker) {
        this.enabledInvoker = enabledInvoker;
    }
}
