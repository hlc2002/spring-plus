package com.hlc.springplus.context.postprocessor;

/**
 * @author : spring
 * {@code @description:}
 * {@code @date} : 2024/2/4
 * {@code @modified} By: spring
 * {@code @project:} spring-plus
 */
public interface BeanPostprocessor {
    default Object beforeInitialization(Object bean, String beanName) {
        return bean;
    }

    default Object afterInitialization(Object bean, String beanName) {
        return bean;
    }
}
