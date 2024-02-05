package com.hlc.springplus.context;

import com.hlc.springplus.bean.BeanDefinition;
import com.hlc.springplus.bean.annotation.AutoWired;
import com.hlc.springplus.bean.annotation.Component;
import com.hlc.springplus.bean.annotation.Lazy;
import com.hlc.springplus.bean.annotation.Scope;
import com.hlc.springplus.context.aware.ApplicationContextAware;
import com.hlc.springplus.context.aware.Aware;
import com.hlc.springplus.context.lifecycle.InitializingBean;
import com.hlc.springplus.context.postprocessor.BeanPostprocessor;
import com.hlc.springplus.core.SpringApplication;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
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
    private List<BeanPostprocessor> beanPostprocessorList = new LinkedList<>();

    public DefaultApplicationContext(Class<?> appconfig) {
        this.appconfig = appconfig;
        //1、扫描启动类注解的字节码列表
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


    /**
     * 扫描bean的字节码列表
     *
     * @param beanClazzList bean的字节码列表(待填充)
     */
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
                throw new RuntimeException("Annotation SpringApplication is not exist");
            }
        } else {
            throw new RuntimeException("Annotation SpringApplication is not exist and appconfig is null");
        }
    }

    /**
     * 加载bean的字节码列表并过滤
     *
     * @param beanClazzList bean的字节码列表(待填充)
     * @param item          文件或文件夹
     */
    private void loadAndFilterBeanClazzes(List<Class<?>> beanClazzList, File item) {
        int begin = item.getAbsolutePath().indexOf("com");
        int end = item.getAbsolutePath().indexOf('.');

        String className = item.getAbsolutePath().substring(begin, end).replace('\\', '.');

        try {
            Class<?> clazz = Class.forName(className);
            if (clazz.isAnnotationPresent(Component.class)) {
                beanClazzList.add(clazz);
                //收集后置处理器（意图是收集后置处理器而不是收集bean对象）
                if (BeanPostprocessor.class.isAssignableFrom(clazz)) {
                    beanPostprocessorList.add((BeanPostprocessor) clazz.getDeclaredConstructor().newInstance());
                }
            }
        } catch (Exception e) {

            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 注册bean的BeanDefinition初始配置信息
     *
     * @param beanClazzList     bean的字节类型列表
     * @param beanDefinitionMap bean的BeanDefinition初始配置信息池子
     */
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

    /**
     * 实例化单例bean
     *
     * @param beanDefinitionMap bean定义信息
     * @param singletonBeanMap  单例bean池子
     */
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
                        if (null == beanName || beanName.isEmpty()) {
                            beanName = clazz.getSimpleName();
                        }
                        //1、实例化bean
                        Object newInstance = clazz.getDeclaredConstructor().newInstance();
                        //2、属性填充
                        attributeAutoWiredPadding(clazz, newInstance);
                        //3、aware能力透传
                        awareBeanInstancePadding(newInstance);
                        //4、初始化
                        //4.1、后置处理器 初始化前执行
                        for (BeanPostprocessor beanPostprocessor : beanPostprocessorList) {
                            newInstance = beanPostprocessor.beforeInitialization(newInstance, beanName);
                        }
                        //4.2、初始化bean执行
                        initializeBeanInstancePadding(newInstance);
                        //4.3、后置处理器能力 初始化后执行
                        for (BeanPostprocessor beanPostprocessor : beanPostprocessorList) {
                            newInstance = beanPostprocessor.afterInitialization(newInstance, beanName);
                        }

                        singletonBeanMap.put(beanName, newInstance);
                    } catch (InvocationTargetException | NoSuchMethodException | InstantiationException |
                             IllegalAccessException e) {
                        throw new RuntimeException(e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * bean的属性填充
     *
     * @param beanClazz   bean的字节类型
     * @param newInstance 实例化的bean
     */
    private void attributeAutoWiredPadding(Class<?> beanClazz, Object newInstance) {
        if (null != beanClazz) {
            Field[] fields = beanClazz.getDeclaredFields();

            for (Field field : fields) {

                if (field.isAnnotationPresent(AutoWired.class)) {

                    field.setAccessible(true);
                    Class<?> declaringClass = field.getType();

                    AutoWired autoWired = field.getAnnotation(AutoWired.class);
                    String name = autoWired.value();

                    if (null == name || name.isEmpty()) {
                        name = declaringClass.getSimpleName();
                    }

                    Object fieldBean = singletonBeanMap.get(name);

                    if (null == fieldBean) {

                        List<Class<?>> beanClazzList = new LinkedList<>();
                        beanClazzList.add(declaringClass);
                        initBeanDefinition(beanClazzList, beanDefinitionMap);

                        Map<String, BeanDefinition> definitionMap = new HashMap<>();
                        definitionMap.put(name, beanDefinitionMap.get(name));
                        instanceSingletonBeans(definitionMap, singletonBeanMap);

                        try {
                            field.set(newInstance, singletonBeanMap.get(name));
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e.getMessage());
                        }
                    } else {

                        try {
                            field.set(newInstance, fieldBean);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e.getMessage());
                        }
                    }
                }
            }
        }
    }

    /**
     * bean的Aware接口的实现类填充
     *
     * @param bean bean实例对象
     */
    private void awareBeanInstancePadding(Object bean) {
        if (null != bean) {
            if (bean instanceof Aware) {
                if (bean instanceof ApplicationContextAware) {
                    ((ApplicationContextAware) bean).setApplicationContext(this);
                }
            }
        }
    }

    /**
     * bean的初始化方法填充
     *
     * @param bean 实例化的bean
     */
    private void initializeBeanInstancePadding(Object bean) {
        if (null != bean) {
            if (bean instanceof InitializingBean) {
                ((InitializingBean) bean).afterPropertiesSet();
            }
        }
    }

    private void sortBeanInstanceClazzList() {

    }
}
