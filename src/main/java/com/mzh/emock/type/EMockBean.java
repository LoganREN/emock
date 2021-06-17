package com.mzh.emock.type;


import org.springframework.core.Ordered;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EMockBean {
    int order() default Ordered.LOWEST_PRECEDENCE;
    InjectPoint injectPoint() default InjectPoint.AFTER_APPLICATION_READY;
    Class<?>[] injectExclude() default {};
    enum InjectPoint{
        AFTER_APPLICATION_READY,
        AFTER_BEAN_INITIALIZATION
    }
}
