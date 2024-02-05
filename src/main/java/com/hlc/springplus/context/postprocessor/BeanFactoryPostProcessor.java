package com.hlc.springplus.context.postprocessor;

import com.hlc.springplus.factory.ConfigurableBeanFactory;

/**
 * @author : spring
 * {@code @description:}
 * {@code @date} : 2024/2/5
 * {@code @modified} By: spring
 * {@code @project:} spring-plus
 */
public interface BeanFactoryPostProcessor {
    void postProcessBeanFactory(ConfigurableBeanFactory beanFactory);
}
