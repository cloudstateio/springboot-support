package io.cloudstate.springboot.starter.internal;

import io.cloudstate.javasupport.CloudState;
import io.cloudstate.springboot.starter.internal.scan.CloudstateEntityScan;
import io.cloudstate.springboot.starter.internal.scan.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public final class CloudstateUtils {
    private static final Logger LOG = LoggerFactory.getLogger(CloudstateUtils.class);

    public static CloudState register(CloudstateEntityScan entityScan) {
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
}
