package com.hlc.springplus.context;

import com.hlc.springplus.factory.BeanFactory;

/**
 * @author : spring
 * {@code @description:}
 * {@code @date} : 2024/2/4
 * {@code @modified} By: spring
 * {@code @project:} spring-plus
 */
public interface ApplicationContext extends BeanFactory {
    Object getBean(String beanName);

    <T> void registerBean(T bean, Class<T> clazz, String name);
}
