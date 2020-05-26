package io.cloudstate.springboot.starter.internal;

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
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CloudstateEntityScan implements EntityScan {
    private static final Logger log = LoggerFactory.getLogger(CloudstateEntityScan.class);

    private final ApplicationContext context;
    private final CloudstateProperties properties;
    private final ClassGraph classGraph;
    private List<Entity> entities;

    public CloudstateProperties getProperties() {
        return properties;
    }

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
        if(Objects.nonNull(entities) && !entities.isEmpty()) {
            return entities;
        }

        Instant now = Instant.now();
        List<Entity> crdtEntities = getCrdtDescriptors();
        List<Entity> eventSourcedEntities = getEventSourcedDescriptors();
        log.debug("Found {} Entity(ies) in {}", (crdtEntities.size() + eventSourcedEntities.size()), Duration.between(now, Instant.now()));

        if(crdtEntities.isEmpty() && eventSourcedEntities.isEmpty()) {
            log.warn("No declared descriptor");
        }

        entities = Stream.concat(crdtEntities.stream(), eventSourcedEntities.stream())
                .collect(Collectors.toList());

        return entities;
    }

    private List<Entity> getCrdtDescriptors() {
        return getEntities(CrdtEntity.class);
    }

    private List<Entity> getEventSourcedDescriptors() {
        return getEntities(EventSourcedEntity.class);
    }

    private List<Class<?>> getClassAnnotationWith(Class<? extends Annotation> annotationType) {
        if ( Objects.nonNull(this.properties) && Objects.nonNull(properties.getUserFunctionPackageName()) ) {
            log.debug("Scanner set for {}", properties.getUserFunctionPackageName());
            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AnnotationTypeFilter(annotationType));
            Set<BeanDefinition> definitions = scanner.findCandidateComponents(this.properties.getUserFunctionPackageName());

            return definitions.stream()
                    .map(getBeanDefinitionClass())
                    .filter(this::isaEntityFunction)
                    .collect(Collectors.toList());
        } else {
            try (ScanResult result = classGraph.scan()) {
                return result.getClassesWithAnnotation(annotationType.getName()).loadClasses();
            }
        }

    }

    private boolean isaEntityFunction(Class<?> t) {
        return !EmptyCLass.class.getSimpleName().equals(t.getClass().getSimpleName());
    }

    private Function<BeanDefinition, Class<?>> getBeanDefinitionClass() {
        return beanDefinition -> {
            String className = beanDefinition.getBeanClassName();
            String packageName = className.substring(0,className.lastIndexOf('.'));
            log.debug("PackageName: {} ClassName: {}", packageName, className);
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                log.error("Error during entity function discovery phase", e);
            }
            return EmptyCLass.class;
        };
    }

    private String decaptalized(String string) {
        char c[] = string.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }

    private List<Entity> getEntities(Class<? extends Annotation> annotationType) {
        final List<Class<?>> eventSourcedEntities = getClassAnnotationWith(annotationType);
        return eventSourcedEntities.stream()
                .map(this::apply)
                .collect(Collectors.toList());
    }

    private Entity apply(Class<?> entity) {
        Descriptors.ServiceDescriptor descriptor = null;
        Descriptors.FileDescriptor[] additionalDescriptors = null;

        if (isaBeanDescriptorsType()) {
            // Then verify if descriptor already declared with bean
            descriptor = tryGetServiceDescriptor(entity, descriptor);

            // Then verify if descriptor already declared with bean
            additionalDescriptors = tryGetFileDescriptors(entity, additionalDescriptors);

        } else {

            for (Method method : entity.getDeclaredMethods()) {

                if (method.isAnnotationPresent(EntityServiceDescriptor.class)) {
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
        }

        Entity entityType = new Entity(EntityType.EventSourced, entity, descriptor, additionalDescriptors);
        log.debug("Registering Entity -> {}", entityType);
        return entityType;
    }

    private Descriptors.FileDescriptor[] tryGetFileDescriptors(Class<?> entity, Descriptors.FileDescriptor[] additionalDescriptors) {
        try {
            String fileDescriptorBeanName = decaptalized(entity.getSimpleName() + "FileDescriptors");
            log.trace("Trying bind the ServiceDescriptor {}", fileDescriptorBeanName);
            additionalDescriptors = (Descriptors.FileDescriptor[]) this.context
                    .getBean(fileDescriptorBeanName);
        }catch (Exception nbde) {
            if (nbde instanceof NoSuchBeanDefinitionException) {
                log.trace("No FileDescriptor Found");
            }
        }
        return additionalDescriptors;
    }

    private Descriptors.ServiceDescriptor tryGetServiceDescriptor(Class<?> entity, Descriptors.ServiceDescriptor descriptor) {
        try{
            String serviceDescriptorBeanName = decaptalized(entity.getSimpleName() + "ServiceDescriptor");
            log.trace("Trying bind the ServiceDescriptor {}", serviceDescriptorBeanName);
            descriptor = (Descriptors.ServiceDescriptor) this.context
                    .getBean(serviceDescriptorBeanName);
        } catch (Exception nbde) {
            if (nbde instanceof NoSuchBeanDefinitionException) {
                log.trace("No ServiceDescriptor Found");
            }
        }
        return descriptor;
    }

    private boolean isaBeanDescriptorsType() {
        return Objects.nonNull(properties.getUserFunctionPackageName()) &&
                !properties.getUserFunctionPackageName().isEmpty();
    }

    final class EmptyCLass {}

}

