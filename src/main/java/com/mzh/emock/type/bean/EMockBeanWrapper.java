package com.mzh.emock.type.bean;
@FunctionalInterface
public interface EMockBeanWrapper<T> {
    T wrap(T t);
}
