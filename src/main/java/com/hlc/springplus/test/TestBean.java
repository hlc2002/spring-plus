package com.hlc.springplus.test;

import com.hlc.springplus.bean.annotation.AutoWired;
import com.hlc.springplus.bean.annotation.Component;
import com.hlc.springplus.bean.annotation.Scope;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;

/**
 * @author : spring
 * {@code @description:}
 * {@code @date} : 2024/2/4
 * {@code @modified} By: spring
 * {@code @project:} spring-plus
 */
@Component(name = "test")
@Scope(value = "singleton")
public class TestBean implements InstantiationAwareBeanPostProcessor {

    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        return InstantiationAwareBeanPostProcessor.super.postProcessBeforeInstantiation(beanClass, beanName);
    }

    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        return InstantiationAwareBeanPostProcessor.super.postProcessAfterInstantiation(bean, beanName);
    }

    @AutoWired
    private TestBean1 testBean1;

    private String content = "test";

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public TestBean() {
    }

    public TestBean(String content) {
        this.content = content;
    }

    public void test() {
        testBean1.test();
    }
}
