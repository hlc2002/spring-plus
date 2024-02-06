package com.hlc.springplus.aspect;

import com.hlc.springplus.aspect.proxy.AbstractAutoProxyCreator;

import java.lang.reflect.Method;
import java.net.URL;

/**
 * @author : spring
 * {@code @description:}
 * {@code @date} : 2024/2/6
 * {@code @modified} By: spring
 * {@code @project:} spring-plus
 */
public class DefaultAutoProxyImpl extends AbstractAutoProxyCreator{

    @Override
    public Object beforeInitialization(Object bean, String beanName) {
        return super.beforeInitialization(bean, beanName);
    }

    @Override
    public Object afterInitialization(Object bean, String beanName) {
        return super.afterInitialization(bean, beanName);
    }

    private Boolean verifyExistProxy(Object bean){
        return bean.getClass().isAnnotationPresent(Aspect.class);
    }
    private void beforeProxyHandle(Object bean){
        Method[] declaredMethods = bean.getClass().getDeclaredMethods();
        for (Method method : declaredMethods) {
            if (method.isAnnotationPresent(Before.class)) {
                method.setAccessible(true);
                Before before = method.getAnnotation(Before.class);

                String value = before.value();
                int index = value.lastIndexOf('.');

                String proxyMethodName = value.substring(index + 1);
                String proxyClassPath = value.substring(0, index);
                String path = proxyClassPath.replace('.', '/');

                ClassLoader classLoader = DefaultAutoProxyImpl.class.getClassLoader();
                URL resource = classLoader.getResource(path);



            }
        }
    }
}
