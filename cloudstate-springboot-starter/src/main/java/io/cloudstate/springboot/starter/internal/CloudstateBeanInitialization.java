package io.cloudstate.springboot.starter.internal;

import akka.Done;
import io.cloudstate.javasupport.CloudState;
import io.cloudstate.springboot.starter.autoconfigure.CloudstateProperties;
import io.cloudstate.springboot.starter.autoconfigure.RegistrarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public final class CloudstateBeanInitialization {
    private static final Logger log = LoggerFactory.getLogger(CloudstateBeanInitialization.class);

    private final CloudState cloudState;
    private final CloudstateProperties properties;
    private final RegistrarService serviceRegister;

    private static ExecutorService workerThreadService =
            Executors.newFixedThreadPool(1, new CustomizableThreadFactory("cloudstate-t"));

    @Autowired
    public CloudstateBeanInitialization(
            CloudState cloudState,
            RegistrarService serviceRegister,
            CloudstateProperties properties) {
        this.cloudState = cloudState;
        this.properties = properties;
        this.serviceRegister = serviceRegister;
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Runnable worker = () -> {
            final Instant start = Instant.now();
            log.info("Starting Cloudstate Server...");
            try {
                if (isAutoRegister()) {
                    serviceRegister.registerAllEntities();
                } else {
                    log.warn("AutoRegister is set to FALSE. Then Entities must be registered manually");
                }

                if (isAutoStartup()) {
                    cloudState
                            .start()
                            .toCompletableFuture()
                            .exceptionally(ex -> {
                                log.error("Failure on Cloudstate Server startup", ex);
                                return Done.done();
                            }).thenAccept(done -> {
                        Duration timeElapsed = Duration.between(start, Instant.now());
                        log.info("Cloudstate Server keep alive for {}", timeElapsed);
                    }).get();
                } else {
                    log.warn("AutoStartup is set to FALSE. Then Cloudstate must be started manually");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        workerThreadService.execute(worker);
    }

    @EventListener
    public void onApplicationEvent(ContextClosedEvent event) {
        if (!workerThreadService.isShutdown() || !workerThreadService.isTerminated()) {
            workerThreadService.shutdown();
        }
    }

    private boolean isAutoRegister() {
        return Objects.nonNull(properties) && properties.isAutoRegister();
    }

    private boolean isAutoStartup() {
        return Objects.nonNull(properties) && properties.isAutoStartup();
    }

}
