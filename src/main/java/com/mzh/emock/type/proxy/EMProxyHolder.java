package com.mzh.emock.type.proxy;

public class EMProxyHolder {
    private Class<?> clz;
    private Object oldBean;
    private Object proxy;

    public EMProxyHolder(Object oldBean, Class<?> clz, Object proxy) {
        this.oldBean=oldBean;
        this.clz = clz;
        this.proxy = proxy;
    }

    public boolean matched(Class<?> clz,Object oldBean) {
        return this.clz==clz && this.oldBean==oldBean;
    }

    public Class<?> getClz() {
        return clz;
    }

    public void setClz(Class<?> clz) {
        this.clz = clz;
    }

    public Object getProxy() {
        return proxy;
    }

    public void setProxy(Object proxy) {
        this.proxy = proxy;
    }

    public Object getOldBean() {
        return oldBean;
    }

    public void setOldBean(Object oldBean) {
        this.oldBean = oldBean;
    }
}
