package com.hlc.springplus.test;

import com.hlc.springplus.bean.annotation.Component;
import com.hlc.springplus.bean.annotation.Lazy;
import com.hlc.springplus.bean.annotation.Scope;

/**
 * @author : spring
 * {@code @description:}
 * {@code @date} : 2024/2/4
 * {@code @modified} By: spring
 * {@code @project:} spring-plus
 */
@Component(name = "test")
@Scope(value = "singleton")
public class TestBean {
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
}
