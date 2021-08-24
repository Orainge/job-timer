package com.orainge.tools.jobtimer.util;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * SpringBoot 容器 Bean 工具类
 *
 * @author orainge
 * @since 2021/6/10
 */
@Component
@ConditionalOnMissingBean({BeanUtils.class})
public class BeanUtils {
    /**
     * 将管理上下文的applicationContext设置成静态变量，供全局调用
     */
    public static ApplicationContext applicationContext;

    @Resource
    public ApplicationContext applicationContextBean;

    @PostConstruct
    public void init() {
        applicationContext = this.applicationContextBean;
    }

    /**
     * 通过 name 获取 Bean
     *
     * @param name Bean 的名称
     * @return Bean
     */
    public static Object getBean(String name) {
        return applicationContext.getBean(name);
    }

    /**
     * 通过 class 获取 Bean
     *
     * @param clazz class 类型
     * @return Bean
     */
    public static <T> T getBeanByClass(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    /**
     * 通过 name 以及 Clazz 返回指定的 Bean
     *
     * @param name  Bean 的名称
     * @param clazz class 类型
     * @return Bean
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return applicationContext.getBean(name, clazz);
    }

    /**
     * 判断是否包含名称为 name 的 Bean
     *
     * @param name Bean 的名称
     * @return true: 存在; false: 不存在
     */
    public static boolean containsBean(String name) {
        return applicationContext.containsBean(name);
    }
}