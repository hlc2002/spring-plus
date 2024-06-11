package com.hlc.springplus.context;

import com.hlc.springplus.bean.BeanDefinition;
import com.hlc.springplus.bean.annotation.*;
import com.hlc.springplus.context.aware.ApplicationContextAware;
import com.hlc.springplus.context.aware.Aware;
import com.hlc.springplus.context.aware.BeanNameAware;
import com.hlc.springplus.context.lifecycle.InitializingBean;
import com.hlc.springplus.context.postprocessor.BeanPostprocessor;
import com.hlc.springplus.core.SpringApplication;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    private final Class<?> appConfig;

    private final Map<String, Object> singletonBeanMap = new ConcurrentHashMap<>(256);

    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);
    private final List<BeanPostprocessor> beanPostprocessorList = new LinkedList<>();

    public DefaultApplicationContext(Class<?> appconfig) {
        // 传入启动类加载启动类上的配置
        this.appConfig = appconfig;
        //1、扫描启动类注解的字节码列表
        List<Class<?>> beanClazzList = new LinkedList<>();
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
        //扫描启动类注解
        if (null != appConfig && appConfig.isAnnotationPresent(SpringApplication.class)) {
            //获取spring应用配置注解对象的值
            SpringApplication annotation = appConfig.getAnnotation(SpringApplication.class);
            if (null != annotation) {
                //根据注解中的scanPackagePath属性的值加载目录下的资源文件
                String beanPackagePath = annotation.scanBeanPackagePath();
                //我们的路径是以.分隔的，需要转换成以/分隔
                String path = beanPackagePath.replace('.', '/');
                //获取当前类加载器
                ClassLoader classLoader = DefaultApplicationContext.class.getClassLoader();
                //使用类加载器加载资源文件
                URL resource = classLoader.getResource(path);

                if (resource != null) {
                    File file = new File(resource.getFile());
                    if (file.isDirectory()) {
                        //获取文件列表中的全部文件
                        File[] files = file.listFiles();
                        if (files != null) {
                            for (File item : files) {
                                //加载列表中需要注入容器的Bean字节码列表
                                loadAndFilterBeanClazzes(beanClazzList, item);
                            }
                        }
                    } else {
                        //是文件则直接加载它需要注入容器的Bean
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
        //获取类名（注意替换文件绝对路径的分隔字符）
        String className = item.getAbsolutePath().substring(begin, end).replace('\\', '.');

        try {
            //反射加载字节码
            Class<?> clazz = Class.forName(className);
            //是否是标注了组件注解的字节码
            if (clazz.isAnnotationPresent(Component.class)) {
                //如果是则存储在字节码列表中
                beanClazzList.add(clazz);
                //收集后处理器（意图是收集后处理器而不是收集bean对象）
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
                beanDefinition.setLazy(null != lazy ? Boolean.TRUE : Boolean.FALSE);
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
                        //检查是否实现初始化Bean的接口
                        initializeBeanInstancePadding(newInstance);
                        //检查是否配置过init方法
                        initBeanMethodInstancePadding(newInstance);
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
                if (bean instanceof BeanNameAware) {
                    ((BeanNameAware) bean).setBeanName();
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

    /**
     * bean的初始化方法填充
     *
     * @param newInstance 实例化的bean
     */
    private void initBeanMethodInstancePadding(Object newInstance) {
        if (null != newInstance) {
            Method[] methods = newInstance.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(InitBeanMethod.class)) {
                    method.setAccessible(true);
                    try {
                        method.invoke(newInstance);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e.getMessage());
                    }
                }
            }
        }
    }
}
