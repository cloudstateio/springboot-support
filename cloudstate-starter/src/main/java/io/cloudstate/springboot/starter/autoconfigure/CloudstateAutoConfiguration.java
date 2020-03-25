package io.cloudstate.springboot.starter.autoconfigure;

import akka.Done;
import io.cloudstate.javasupport.CloudState;
import io.cloudstate.springboot.starter.CloudstateEntityScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutionException;

@Configuration
@ConditionalOnClass(CloudstateProperties.class)
@EnableConfigurationProperties(CloudstateProperties.class)
public class CloudstateAutoConfiguration {

    @Autowired
    private CloudstateProperties cloudstateProperties;

    @Bean
    @ConditionalOnMissingBean
    public CloudstateEntityScan cloudstateEntityScan(){
        return new CloudstateEntityScan(cloudstateProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public Done cloudState(CloudstateEntityScan entityScan) throws ExecutionException, InterruptedException {
        return registerEntities(entityScan)
                .start()
                .toCompletableFuture()
                .get();
    }

    private CloudState registerEntities(CloudstateEntityScan entityScan) {
        CloudState cloudState = new CloudState();
        entityScan.findEntities().forEach(entity -> {

            switch (entity.getEntityType()) {
                case EventSourced:
                    //cloudState.registerEventSourcedEntity();
                    break;
                case CRDT:
                    //cloudState.registerCrdtEntity();
                    break;
                default: throw new IllegalArgumentException("Unknown entity type " + entity.getEntityType());
            }
        });
        return cloudState;
    }
}
