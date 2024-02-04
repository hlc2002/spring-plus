package com.hlc.springplus.test;

import com.hlc.springplus.bean.annotation.Component;

/**
 * @author : spring
 * {@code @description:}
 * {@code @date} : 2024/2/4
 * {@code @modified} By: spring
 * {@code @project:} spring-plus
 */
@Component(name = "test")
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

    @Override
    public String toString() {
        return "TestBean{" +
                "content='" + content + '\'' +
                '}';
    }
}
