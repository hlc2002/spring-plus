package com.hlc.springplus.context.aware;

/**
 * @author : spring
 * {@code @description:}
 * {@code @date} : 2024/2/5
 * {@code @modified} By: spring
 * {@code @project:} spring-plus
 */
public interface BeanNameAware extends Aware{
    void setBeanName();
}
