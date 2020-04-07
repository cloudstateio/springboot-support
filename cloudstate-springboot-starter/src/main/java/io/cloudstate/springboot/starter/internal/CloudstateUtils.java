package io.cloudstate.springboot.starter.internal;

import com.google.protobuf.Descriptors;
import io.cloudstate.javasupport.CloudState;
import io.cloudstate.javasupport.Context;
import io.cloudstate.javasupport.EntityId;
import io.cloudstate.javasupport.EntitySupportFactory;
import io.cloudstate.javasupport.eventsourced.EventSourcedEntity;
import io.cloudstate.javasupport.impl.AnySupport;
import io.cloudstate.javasupport.impl.crdt.AnnotationBasedCrdtExtensionSupport;
import io.cloudstate.javasupport.impl.eventsourced.AnnotationBasedEventSourcedExtensionSupport;
import io.cloudstate.springboot.starter.CloudstateContext;
import io.cloudstate.springboot.starter.autoconfigure.CloudstateProperties;
import io.cloudstate.springboot.starter.internal.scan.CloudstateEntityScan;
import io.cloudstate.springboot.starter.internal.scan.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public final class CloudstateUtils {
    private static final Logger LOG = LoggerFactory.getLogger(CloudstateUtils.class);
    public static final String CLOUDSTATE_SPRINGBOOT_SUPPORT = "cloudstate-springboot-support";

    public static CloudState register(
            CloudState cloudState,
            ThreadLocal<Map<Class<?>, Map<String, Object>>> stateController,
            ApplicationContext applicationContext,
            CloudstateEntityScan entityScan,
            CloudstateProperties properties) throws Exception {

        // Setting environments before create Cloudstate server
        setServerOptions(properties);
        final List<Entity> entities = entityScan.findEntities();

        if (Objects.nonNull(entities) && !entities.isEmpty()){

            entities.forEach(entity -> {
                EntitySupportFactory entitySupportFactory = new BaseEntitySupportFactory(
                        entity, applicationContext, stateController);

                Class<?> entityClass = entitySupportFactory.typeClass();
                final AnySupport anySupport = newAnySupport(entity.getAdditionalDescriptors());

                if (Objects.nonNull(entity.getDescriptor())) {
                    switch (entity.getEntityType()) {
                        case EventSourced:
                            cloudState.registerEventSourcedEntity(
                                    new AnnotationBasedEventSourcedExtensionSupport(
                                            entitySupportFactory, anySupport, entity.getDescriptor()),
                                    entity.getDescriptor(),
                                    getPersistenceId(entityClass),
                                    getSnapshotEvery(entityClass),
                                    entity.getAdditionalDescriptors()
                            );

                            break;
                        case CRDT:
                            cloudState.registerCrdtEntity(
                                    new AnnotationBasedCrdtExtensionSupport(
                                            entitySupportFactory, anySupport, entity.getDescriptor()),
                                    entity.getDescriptor(),
                                    entity.getAdditionalDescriptors());

                            break;
                        default:
                            throw new IllegalArgumentException(
                                    String.format("Unknown entity type %s", entity.getEntityType()));
                    }
                } else {
                    LOG.warn("Entity '{}' was found but no valid ServiceDescriptor was declared",
                            entity.getEntityClass().getName());
                }
            });
        }
        return cloudState;
    }

    public static CloudState register(
            Object object,
            CloudState cloudState,
            ThreadLocal<Map<Class<?>, Map<String, Object>>> stateController,
            ApplicationContext applicationContext,
            CloudstateEntityScan entityScan,
            CloudstateProperties properties) throws Exception {

        // Setting environments before create Cloudstate server
        setServerOptions(properties);
        final List<Entity> entities = entityScan.findEntities()
                .stream()
                .filter(e -> e.getEntityClass() == object.getClass())
                .collect(Collectors.toList());

        if (Objects.nonNull(entities) && !entities.isEmpty()){

            entities.forEach(entity -> {
                EntitySupportFactory entitySupportFactory = new BaseEntitySupportFactory(
                        entity, applicationContext, stateController);

                Class<?> entityClass = entitySupportFactory.typeClass();
                final AnySupport anySupport = newAnySupport(entity.getAdditionalDescriptors());

                if (Objects.nonNull(entity.getDescriptor())) {
                    switch (entity.getEntityType()) {
                        case EventSourced:
                            cloudState.registerEventSourcedEntity(
                                    new AnnotationBasedEventSourcedExtensionSupport(
                                            entitySupportFactory, anySupport, entity.getDescriptor()),
                                    entity.getDescriptor(),
                                    getPersistenceId(entityClass),
                                    getSnapshotEvery(entityClass),
                                    entity.getAdditionalDescriptors()
                            );

                            break;
                        case CRDT:
                            cloudState.registerCrdtEntity(
                                    new AnnotationBasedCrdtExtensionSupport(
                                            entitySupportFactory, anySupport, entity.getDescriptor()),
                                    entity.getDescriptor(),
                                    entity.getAdditionalDescriptors());

                            break;
                        default:
                            throw new IllegalArgumentException(
                                    String.format("Unknown entity type %s", entity.getEntityType()));
                    }
                } else {
                    LOG.warn("Entity '{}' was found but no valid ServiceDescriptor was declared",
                            entity.getEntityClass().getName());
                }
            });
        }
        return cloudState;
    }

    public static void postConstructObject(
            ThreadLocal<Map<Class<?>, Map<String, Object>>> stateController,
            Class<?> entityClass,
            Context eventSourcedEntityCreationContext,
            String entityId){

        Map<String, Object> state = new HashMap<>();
        final Field[] fields = entityClass.getDeclaredFields();
        for (Field field: fields){
            LOG.trace("Field: {}", field);
            setEntityId(stateController, state, entityClass,entityId, field);
            setCloudstateContext(stateController, state, entityClass, eventSourcedEntityCreationContext, field);
        }

    }

    public static void setEntityId(ThreadLocal<Map<Class<?>, Map<String, Object>>> stateController,
                                   Map<String, Object> state, Class<?> entityClass,
                                   String entityId, Field field) {
        if (field.isAnnotationPresent(EntityId.class)) {
            if (field.getType().equals(String.class)) {
                LOG.debug("Set the EntityId: {}", entityId);
                state.put(field.getName(), entityId);
                stateController.get().put(entityClass, state);
            } else {
                LOG.warn("Type of Field annotated with @EntityId must be String.class");
            }
        }
    }

    public static void setCloudstateContext(ThreadLocal<Map<Class<?>, Map<String, Object>>> stateController,
                                            Map<String, Object> state, Class<?> entityClass,
                                            Context eventSourcedEntityCreationContext,
                                            Field field) {
        if (field.isAnnotationPresent(CloudstateContext.class) && Objects.nonNull(eventSourcedEntityCreationContext)) {
            field.setAccessible(true);
            LOG.debug("Set the EventSourcedEntityCreationContext: {}",
                    eventSourcedEntityCreationContext.getClass().getSimpleName());
            state.put(field.getName(), eventSourcedEntityCreationContext);
            stateController.get().put(entityClass, state);
        }
    }

    private static String getPersistenceId(Class<?> entityClass) {
        EventSourcedEntity ann = entityClass.getAnnotation(EventSourcedEntity.class);
        String p = Optional.ofNullable(ann.persistenceId()).orElse("");
        return ( p.trim().isEmpty() ? entityClass.getSimpleName() : p );
    }

    private static int getSnapshotEvery(Class<?> entityClass) {
        EventSourcedEntity ann = entityClass.getAnnotation(EventSourcedEntity.class);
        return ann.snapshotEvery();
    }

    private static AnySupport newAnySupport(Descriptors.FileDescriptor[] descriptors) {
        return new AnySupport(
                descriptors,
                CloudstateUtils.class.getClassLoader(),
                AnySupport.DefaultTypeUrlPrefix(),
                AnySupport.PREFER_JAVA());
    }

    private static void setServerOptions(CloudstateProperties properties) throws Exception {
        // This is workaround to 0.4.3 java-support.
        // In upcoming releases this should be resolved via HOCON config instance directly
        Map<String, String> props = new HashMap<>();

        if (!properties.USER_FUNCTION_INTERFACE_DEFAULT.equals(properties.getUserFunctionInterface())) {
            props.put("HOST", properties.getUserFunctionInterface());
        }

        if (properties.USER_FUNCTION_PORT != properties.getUserFunctionPort()) {
            props.put("PORT", String.valueOf(properties.getUserFunctionPort()));
        }

        if (!props.isEmpty()) {
            props.put("SUPPORT_LIBRARY_NAME", CLOUDSTATE_SPRINGBOOT_SUPPORT);
            setEnv(Collections.unmodifiableMap(props));
        }
    }

    private static void setEnv(Map<String, String> newenv) throws Exception {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(newenv);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>)     theCaseInsensitiveEnvironmentField.get(null);
            cienv.putAll(newenv);
        } catch (NoSuchFieldException e) {
            Class[] classes = Collections.class.getDeclaredClasses();
            Map<String, String> env = System.getenv();
            for(Class cl : classes) {
                if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Field field = cl.getDeclaredField("m");
                    field.setAccessible(true);
                    Object obj = field.get(env);
                    Map<String, String> map = (Map<String, String>) obj;
                    map.clear();
                    map.putAll(newenv);
                }
            }
        }
    }

}