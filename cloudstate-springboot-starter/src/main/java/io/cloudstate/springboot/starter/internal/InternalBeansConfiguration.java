package io.cloudstate.springboot.starter.internal;

import io.cloudstate.springboot.starter.internal.scan.CloudstateEntityScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

//@Configuration
//@ComponentScan(basePackages = "io.cloudstate.springboot.starter")
public class InternalBeansConfiguration {

/*    @Autowired
    private CloudstateEntityScan cloudstateEntityScan;

    @Autowired
    private ThreadLocal<Map<Class<?>, Map<String, Object>>> stateController;

    @Bean
    public CloudstateBeanPostProcessor cloudstateBeanPostProcessor(){
        return newPostProcessor(cloudstateEntityScan, stateController);
    }

    public static CloudstateBeanPostProcessor newPostProcessor(
            CloudstateEntityScan cloudstateEntityScan, ThreadLocal<Map<Class<?>, Map<String, Object>>> stateController) {
        return CloudstateUtils.newBeanPostProcessor(cloudstateEntityScan, stateController);
    }*/


}
