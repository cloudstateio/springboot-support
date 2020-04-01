package io.cloudstate.springboot.starter.autoconfigure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import io.cloudstate.javasupport.CloudState;
import io.cloudstate.springboot.starter.internal.scan.CloudstateEntityScan;
import static io.cloudstate.springboot.starter.internal.CloudstateUtils.register;

@Configuration
@ConditionalOnClass(CloudstateProperties.class)
@EnableConfigurationProperties(CloudstateProperties.class)
@ComponentScan(basePackages = "io.cloudstate.springboot.starter")
public class CloudstateAutoConfiguration {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private CloudstateProperties cloudstateProperties;

    @Bean
    @ConditionalOnMissingBean
    public CloudstateEntityScan cloudstateEntityScan() {
        return new CloudstateEntityScan(context, cloudstateProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public CloudState cloudState(CloudstateEntityScan entityScan) throws Exception {
        return register(context, entityScan);
    }

}
