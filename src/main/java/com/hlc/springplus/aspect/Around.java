package com.hlc.springplus.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author : spring
 * {@code @description:}
 * {@code @date} : 2024/2/6
 * {@code @modified} By: spring
 * {@code @project:} spring-plus
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Around {
    String value();
}
