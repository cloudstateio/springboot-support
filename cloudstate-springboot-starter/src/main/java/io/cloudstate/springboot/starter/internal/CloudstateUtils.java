package io.cloudstate.springboot.starter.internal;

import io.cloudstate.javasupport.CloudState;
import io.cloudstate.springboot.starter.internal.scan.CloudstateEntityScan;

public final class CloudstateUtils {

    public static CloudState register(CloudstateEntityScan entityScan) {
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
