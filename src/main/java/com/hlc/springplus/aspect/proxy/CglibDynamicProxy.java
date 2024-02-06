package com.hlc.springplus.aspect.proxy;

import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @author : spring
 * {@code @description:}
 * {@code @date} : 2024/2/6
 * {@code @modified} By: spring
 * {@code @project:} spring-plus
 */
public interface CglibDynamicProxy extends MethodInterceptor {
    @Override
    default Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable{
        return obj;
    }
}
