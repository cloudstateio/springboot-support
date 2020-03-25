package io.cloudstate.springboot.starter.autoconfigure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(CloudstateProperties.class)
@EnableConfigurationProperties(CloudstateProperties.class)
public class CloudstateAutoConfiguration {

    @Autowired
    private CloudstateAutoConfiguration cloustateProperties;
}
