package io.cloudstate.springboot.starter.autoconfigure;

import akka.Done;
import io.cloudstate.javasupport.CloudState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;

@Component
public class CloudstateBeanInitialization {
    Logger log = LoggerFactory.getLogger(CloudstateBeanInitialization.class);

    private final CloudState cloudState;

    @Autowired
    public CloudstateBeanInitialization(CloudState cloudState) {
        this.cloudState = cloudState;
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) throws ExecutionException, InterruptedException {
        final Instant start = Instant.now();
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
