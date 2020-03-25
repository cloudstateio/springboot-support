package io.cloudstate.springboot.starter;

import io.cloudstate.springboot.starter.autoconfigure.CloudstateProperties;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CloudstateEntityScan implements EntityScan {

    private final CloudstateProperties properties;

    public CloudstateEntityScan(final CloudstateProperties properties) {
        this.properties = properties;
    }

    public List<Entity> findEntities() {
        List<Entity> crdtEntities = getCrdtDescriptors();
        List<Entity> eventSourcedEntities = getEventSourcedDescriptors();

        return Stream.concat(crdtEntities.stream(), eventSourcedEntities.stream())
                .collect(Collectors.toList());
    }

    private List<Entity> getCrdtDescriptors() {
        return properties.getCrdtDescriptors()
                .entrySet()
                .stream()
                .map(e -> new Entity(EntityType.CRDT, e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private List<Entity> getEventSourcedDescriptors() {
        return properties.getEventSourcedDescriptors()
                .entrySet()
                .stream()
                .map(e -> new Entity(EntityType.EventSourced, e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }
}

