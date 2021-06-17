package com.mzh.emock.type.bean.method;

import java.lang.reflect.InvocationTargetException;

public interface EMockMethodInvoker<R,A> {
    R invoke(SimpleInvoker<R, A> oldMethod, SimpleInvoker<R, A> newMethod, A args);
    String getCode();

    interface SimpleInvoker<R,A>{
        R invoke(A args) throws InvocationTargetException, IllegalAccessException;
    }
}
