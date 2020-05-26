package io.cloudstate.springboot.starter.autoconfigure;

import akka.Done;
import io.cloudstate.javasupport.CloudState;
import io.cloudstate.springboot.starter.internal.CloudstateEntityScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

import static io.cloudstate.springboot.starter.internal.CloudstateUtils.register;

@Component
public class CloudstateBeanInitialization {
    Logger log = LoggerFactory.getLogger(CloudstateBeanInitialization.class);

    private final CloudState cloudState;
    private final ApplicationContext context;
    private final CloudstateEntityScan entityScan;

    @Autowired
    public CloudstateBeanInitialization(ApplicationContext context,CloudstateEntityScan entityScan,  CloudState cloudState) {
        this.context = context;
        this.cloudState = cloudState;
        this.entityScan = entityScan;
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) throws Exception {
        final Instant start = Instant.now();
        register(cloudState, context, entityScan);
        log.info("Starting Cloudstate Server...");
        cloudState.start()
                .toCompletableFuture()
                .exceptionally(ex -> {
                    log.error("Failure on Cloudstate Server startup", ex);
                    return Done.done();
                }).thenAccept(done -> {
            Duration timeElapsed = Duration.between(start, Instant.now());
            log.debug("Cloudstate Server keep alived for {}", timeElapsed);
        });
    }

}