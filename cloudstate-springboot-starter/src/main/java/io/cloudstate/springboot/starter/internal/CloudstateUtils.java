package io.cloudstate.springboot.starter.internal;

import io.cloudstate.javasupport.CloudState;
import io.cloudstate.springboot.starter.autoconfigure.CloudstateProperties;
import io.cloudstate.springboot.starter.internal.scan.CloudstateEntityScan;
import io.cloudstate.springboot.starter.internal.scan.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;

public final class CloudstateUtils {
    private static final Logger LOG = LoggerFactory.getLogger(CloudstateUtils.class);
    public static final String CLOUDSTATE_SPRINGBOOT_SUPPORT = "cloudstate-springboot-support";

    public static CloudState register(CloudstateEntityScan entityScan) throws Exception {
        // Setting environments before create Cloudstate server
        setServerOptions(entityScan);
        CloudState cloudState = new CloudState();
        final List<Entity> entities = entityScan.findEntities();

        if (Objects.nonNull(entities) && !entities.isEmpty()){
            entities.forEach(entity -> {

                if (Objects.nonNull(entity.getDescriptor())) {
                    switch (entity.getEntityType()) {

                        case EventSourced:
                            cloudState.registerEventSourcedEntity(
                                    entity.getEntityClass(),
                                    entity.getDescriptor(),
                                    entity.getAdditionalDescriptors());
                            break;
                        case CRDT:
                            cloudState.registerCrdtEntity(
                                    entity.getEntityClass(),
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

    private static void setServerOptions(CloudstateEntityScan entityScan) throws Exception {
        // This is workaround to 0.4.3 java-support.
        // In upcoming releases this should be resolved via HOCON config instance directly
        CloudstateProperties properties = entityScan.getProperties();
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