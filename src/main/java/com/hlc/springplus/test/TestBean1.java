package com.hlc.springplus.test;

import com.hlc.springplus.bean.annotation.Component;

/**
 * @author : spring
 * {@code @description:}
 * {@code @date} : 2024/2/4
 * {@code @modified} By: spring
 * {@code @project:} spring-plus
 */
@Component
public class TestBean1 {
    public void test() {
        System.out.println("test autowired");
    }
}
