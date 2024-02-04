package com.hlc.springplus.bean;

/**
 * @author : spring
 * {@code @description:}
 * {@code @date} : 2024/2/4
 * {@code @modified} By: spring
 * {@code @project:} spring-plus
 */
public class BeanDefinition {
    private String scope;
    private Boolean isLazy;
    private Class<?> beanClazz;


    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Boolean getLazy() {
        return isLazy;
    }

    public void setLazy(Boolean lazy) {
        isLazy = lazy;
    }

    public Class<?> getBeanClazz() {
        return beanClazz;
    }

    public void setBeanClazz(Class<?> beanClazz) {
        this.beanClazz = beanClazz;
    }
}
