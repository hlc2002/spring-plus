package com.hlc.springplus.context;

import com.hlc.springplus.bean.BeanDefinition;
import com.hlc.springplus.bean.annotation.Component;
import com.hlc.springplus.bean.annotation.Lazy;
import com.hlc.springplus.bean.annotation.Scope;
import com.hlc.springplus.core.SpringApplication;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : spring
 * {@code @description:}
 * {@code @date} : 2024/2/4
 * {@code @modified} By: spring
 * {@code @project:} spring-plus
 */
public class DefaultApplicationContext implements ApplicationContext {
    private final Class<?> appconfig;

    private List<Class<?>> beanClazzList = new LinkedList<>();
    private Map<String, Object> singletonBeanMap = new ConcurrentHashMap<>();

    private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    public DefaultApplicationContext(Class<?> appconfig) {
        this.appconfig = appconfig;
        //1、扫描bean的字节码列表
        scanBeansByPackage(beanClazzList);
        //2、注册bean的BeanDefinition初始配置信息
        initBeanDefinition(beanClazzList, beanDefinitionMap);
        //3、实例化单例bean并存入map中
        instanceSingletonBeans(beanDefinitionMap, singletonBeanMap);
    }

    @Override
    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (null != beanDefinition && "prototype".equals(beanDefinition.getScope())) {
            try {
                return beanDefinition.getBeanClazz().getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        return singletonBeanMap.get(beanName);
    }

    @Override
    public <T> void registerBean(T bean, Class<T> clazz, String name) {
        singletonBeanMap.put(name, bean);
    }

    protected void scanBeansByPackage(List<Class<?>> beanClazzList) {

        if (null != appconfig && appconfig.isAnnotationPresent(SpringApplication.class)) {

            SpringApplication annotation = appconfig.getAnnotation(SpringApplication.class);
            if (null != annotation) {

                String beanPackagePath = annotation.scanBeanPackagePath();
                String path = beanPackagePath.replace('.', '/');

                ClassLoader classLoader = DefaultApplicationContext.class.getClassLoader();
                URL resource = classLoader.getResource(path);

                if (resource != null) {
                    File file = new File(resource.getFile());
                    if (file.isDirectory()) {
                        File[] files = file.listFiles();
                        if (files != null) {
                            for (File item : files) {

                                loadAndFilterBeanClazzes(beanClazzList, item);
                            }
                        }
                    } else {

                        loadAndFilterBeanClazzes(beanClazzList, file);
                    }
                }
            } else {
                throw new RuntimeException("");
            }
        } else {
            throw new RuntimeException("");
        }
    }

    private void loadAndFilterBeanClazzes(List<Class<?>> beanClazzList, File item) {
        int begin = item.getAbsolutePath().indexOf("com");
        int end = item.getAbsolutePath().indexOf('.');

        String className = item.getAbsolutePath().substring(begin, end).replace('\\', '.');

        try {
            Class<?> clazz = Class.forName(className);
            if (clazz.isAnnotationPresent(Component.class)) {
                beanClazzList.add(clazz);
            }
        } catch (Exception e) {

            throw new RuntimeException(e.getMessage());
        }
    }

    private void initBeanDefinition(List<Class<?>> beanClazzList, Map<String, BeanDefinition> beanDefinitionMap) {
        if (null != beanClazzList && !beanClazzList.isEmpty()) {
            for (Class<?> clazz : beanClazzList) {
                BeanDefinition beanDefinition = new BeanDefinition();
                Component component = clazz.getAnnotation(Component.class);
                Scope scope = clazz.getAnnotation(Scope.class);
                Lazy lazy = clazz.getAnnotation(Lazy.class);

                beanDefinition.setBeanClazz(clazz);
                beanDefinition.setLazy(null != lazy);
                beanDefinition.setScope(null != scope ? scope.value() : "prototype");
                String beanName = component.name();
                if (beanName.isEmpty()) {
                    beanName = clazz.getSimpleName();
                }
                beanDefinitionMap.put(beanName, beanDefinition);
            }
        }
    }

    private void instanceSingletonBeans(Map<String, BeanDefinition> beanDefinitionMap, Map<String, Object> singletonBeanMap) {

        if (null != beanDefinitionMap && !beanDefinitionMap.isEmpty()) {
            for (Class<?> clazz : beanDefinitionMap.values().stream().map(BeanDefinition::getBeanClazz).toList()) {
                if (clazz.isAnnotationPresent(Scope.class) && "prototype".equals(clazz.getAnnotation(Scope.class).value())) {
                    continue;
                }
                if (!clazz.isAnnotationPresent(Lazy.class)) {
                    //实例化bean
                    try {
                        Component component = clazz.getAnnotation(Component.class);
                        String beanName = component.name();

                        Object newInstance = clazz.getDeclaredConstructor().newInstance();

                        if (null != beanName && !beanName.isEmpty()) {
                            singletonBeanMap.put(beanName, newInstance);
                        } else {
                            singletonBeanMap.put(clazz.getSimpleName(), newInstance);
                        }
                    } catch (InvocationTargetException | NoSuchMethodException | InstantiationException |
                             IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private void sortBeanInstanceClazzList() {

    }
}
