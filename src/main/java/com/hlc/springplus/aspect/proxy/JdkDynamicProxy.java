package com.hlc.springplus.aspect.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author : spring
 * {@code @description:}
 * {@code @date} : 2024/2/6
 * {@code @modified} By: spring
 * {@code @project:} spring-plus
 */
public interface JdkDynamicProxy extends InvocationHandler {
    @Override
    default Object invoke(Object o, Method method, Object[] objects) throws Throwable{
        return o;
    }
}
