package io.cloudstate.springboot.starter.autoconfigure;

import io.cloudstate.javasupport.CloudState;
import io.cloudstate.springboot.starter.CloudstateEntityScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(CloudstateProperties.class)
@EnableConfigurationProperties(CloudstateProperties.class)
public class CloudstateAutoConfiguration {

    @Autowired
    private CloudstateProperties cloudstateProperties;

    @Bean
    @ConditionalOnMissingBean
    public CloudstateEntityScan cloudstateEntityScan(){
        return null;
    }

    @Bean
    @ConditionalOnMissingBean
    public CloudState cloudState(CloudstateEntityScan entityScan){
        return null;
    }
}
