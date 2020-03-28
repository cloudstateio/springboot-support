package io.cloudstate.springboot.starter.internal.scan;

import com.google.protobuf.Descriptors;
import io.cloudstate.javasupport.crdt.CrdtEntity;
import io.cloudstate.javasupport.eventsourced.EventSourcedEntity;
import io.cloudstate.springboot.starter.EntityAdditionaDescriptors;
import io.cloudstate.springboot.starter.EntityServiceDescriptor;
import io.cloudstate.springboot.starter.autoconfigure.CloudstateProperties;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CloudstateEntityScan implements EntityScan {
    Logger log = LoggerFactory.getLogger(CloudstateEntityScan.class);

    private final ApplicationContext context;
    private final CloudstateProperties properties;
    private final ClassGraph classGraph;

    public CloudstateEntityScan(ApplicationContext context, final CloudstateProperties properties) {
        this.context = context;
        this.properties = properties;
        this.classGraph = new ClassGraph()
                .enableClassInfo()
                .enableAnnotationInfo()
                .blacklistPackages(
                        "org.springframework",
                        "com.typesafe",
                        "com.google",
                        "com.fasterxml",
                        "org.slf4j",
                        "org.eclipse",
                        "com.twitter",
                        "io.spray",
                        "org.reactivestreams",
                        "org.scala",
                        "io.grpc",
                        "io.opencensus",
                        "org.yaml",
                        "io.cloudstate.javasupport");
    }

    public List<Entity> findEntities() {
        Instant now = Instant.now();
        List<Entity> crdtEntities = getCrdtDescriptors();
        List<Entity> eventSourcedEntities = getEventSourcedDescriptors();
        log.debug("Found {} Entity(ies) in {}", (crdtEntities.size() + eventSourcedEntities.size()), Duration.between(now, Instant.now()));

        if(crdtEntities.isEmpty() && eventSourcedEntities.isEmpty()) {
            log.warn("No declared descriptor");
        }

        return Stream.concat(crdtEntities.stream(), eventSourcedEntities.stream())
                .collect(Collectors.toList());
    }

    private List<Entity> getCrdtDescriptors() {
        return getEntities(CrdtEntity.class);
    }

    private List<Entity> getEventSourcedDescriptors() {
        return getEntities(EventSourcedEntity.class);
    }

    private List<Class<?>> getClassAnnotationWith(Class<? extends Annotation> annotationType) {
        try (ScanResult result = classGraph.scan()) {
            return result.getClassesWithAnnotation(annotationType.getName()).loadClasses();
        }
    }

    private List<Entity> getEntities(Class<? extends Annotation> annotationType) {
        final List<Class<?>> eventSourcedEntities = getClassAnnotationWith(annotationType);

        return eventSourcedEntities.stream().map(entity -> {
            Descriptors.ServiceDescriptor descriptor = null;
            Descriptors.FileDescriptor[] additionalDescriptors = null;

            for (Method method: entity.getDeclaredMethods()) {

                if (method.isAnnotationPresent(EntityServiceDescriptor.class)){
                    try {
                        method.setAccessible(true);
                        descriptor = ((Descriptors.ServiceDescriptor)
                                method.invoke(null, null));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        log.error("Failure on load ServiceDescriptor", e);
                    }
                }

                if (method.isAnnotationPresent(EntityAdditionaDescriptors.class)) {
                    try {
                        method.setAccessible(true);
                        additionalDescriptors = (Descriptors.FileDescriptor[]) method.invoke(null, null);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        log.error("Failure on load AdditionalDescriptors", e);
                    }
                }
            }

            Entity entityType = new Entity(EntityType.EventSourced, entity, descriptor, additionalDescriptors);
            log.debug("Registering Entity -> {}", entityType);
            return entityType;
        }).collect(Collectors.toList());
    }
}

