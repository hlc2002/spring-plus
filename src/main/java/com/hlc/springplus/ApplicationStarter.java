package com.hlc.springplus;

import com.hlc.springplus.context.ApplicationContext;
import com.hlc.springplus.context.DefaultApplicationContext;
import com.hlc.springplus.core.SpringApplication;
import com.hlc.springplus.test.TestBean;

/**
 * @author : spring
 * {@code @description:}
 * {@code @date} : 2024/2/4
 * {@code @modified} By: spring
 * {@code @project:} spring-plus
 */
@SpringApplication(scanBeanPackagePath = "com/hlc/springplus/test")
public class ApplicationStarter {
    public static void main(String[] args) {
        ApplicationContext applicationContext = new DefaultApplicationContext(ApplicationStarter.class);
        TestBean contextBean = (TestBean) applicationContext.getBean("test");
        System.out.println(applicationContext.getBean("test"));
        System.out.println(applicationContext.getBean("test"));
        System.out.println(applicationContext.getBean("test"));
    }
}
