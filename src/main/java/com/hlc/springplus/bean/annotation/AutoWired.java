package com.hlc.springplus.bean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author : spring
 * {@code @description:}
 * {@code @date} : 2024/2/4
 * {@code @modified} By: spring
 * {@code @project:} spring-plus
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoWired {
    String value() default "";
}
