package com.hlc.springplus.bean;

/**
 * @author : spring
 * {@code @description:}
 * {@code @date} : 2024/2/4
 * {@code @modified} By: spring
 * {@code @project:} spring-plus
 */
public class BeanDefinition {
    private String name;
    private String scope;
    private Boolean isLazy;
    private Class<?> beanClazz;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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
