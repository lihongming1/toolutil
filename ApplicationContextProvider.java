package com.sftc.isc.middle.taskmanage.conf;

import com.alibaba.fastjson.JSON;
import com.sftc.isc.middle.taskmanage.utils.ManualRegistBeanUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * 用于多线程获取bean
 */
@Configuration
public class ApplicationContextProvider implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext1) throws BeansException {
        applicationContext = applicationContext1;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 通过name获取 Bean.
     *
     * @param name
     * @return
     */
    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }

    /**
     * 通过class获取Bean.
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    /**
     * 通过name,以及Clazz返回指定的Bean
     *
     * @param name
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

    /**
     * 注册bean
     *
     * @param beanName
     * @param beanObject
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T registerBean(String beanName, T beanObject, Class<T> clazz) {
        ApplicationContext applicationContext = getApplicationContext();
        if (applicationContext != null) {
            try{
                Object obj = applicationContext.getBean(beanName, clazz);
                if(obj != null){
                    return applicationContext.getBean(beanName, clazz);
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
            //将applicationContext转换为ConfigurableApplicationContext
            ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) applicationContext;
            // 获取bean工厂并转换为DefaultListableBeanFactory
            DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) configurableApplicationContext.getBeanFactory();
            // 通过BeanDefinitionBuilder创建bean定义
            defaultListableBeanFactory.registerSingleton(beanName, beanObject);
        }
        return applicationContext.getBean(beanName, clazz);
    }
}
