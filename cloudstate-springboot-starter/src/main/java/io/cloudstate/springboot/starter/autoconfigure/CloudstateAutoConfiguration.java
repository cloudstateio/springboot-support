package io.cloudstate.springboot.starter.autoconfigure;

import io.cloudstate.javasupport.CloudState;
import io.cloudstate.springboot.starter.scan.CloudstateEntityScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutionException;

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
    public CloudState cloudState(CloudstateEntityScan entityScan) throws ExecutionException, InterruptedException {
        return registerEntities(entityScan);
    }

    private CloudState registerEntities(CloudstateEntityScan entityScan) {
        CloudState cloudState = new CloudState();
        entityScan.findEntities().forEach(entity -> {
            switch (entity.getEntityType()) {
                case EventSourced:
                    cloudState.registerEventSourcedEntity(entity.getEntityClass(), entity.getDescriptor(), entity.getAdditionalDescriptors());
                    break;
                case CRDT:
                    cloudState.registerCrdtEntity(entity.getEntityClass(), entity.getDescriptor(), entity.getAdditionalDescriptors());
                    break;
                default:
                    throw new IllegalArgumentException(
                            String.format("Unknown entity type %s", entity.getEntityType()));
            }
        });
        return cloudState;
    }
}
