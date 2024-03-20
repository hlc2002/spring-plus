package com.hlc.springplus.core;

import com.hlc.springplus.ApplicationStarter;
import com.hlc.springplus.context.ApplicationContext;
import com.hlc.springplus.context.DefaultApplicationContext;

/**
 * @author : spring
 * {@code @description: spring应用启动器}
 * {@code @date} : 2024/3/20
 * {@code @modified} By: spring
 * {@code @project:} spring-plus
 */
public class SpringApplicationStarter {
    public static void run(Class<?> appStartClazz,String... args){
        //加载容器上下文对象
        ApplicationContext applicationContext = new DefaultApplicationContext(appStartClazz);
    }
}
