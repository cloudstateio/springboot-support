package io.cloudstate.springboot.starter.internal.scan;

import com.google.protobuf.Descriptors;

import java.util.Arrays;
import java.util.Objects;

public final class Entity {

    private final EntityType entityType;
    private final Class entityClass;
    private final Descriptors.ServiceDescriptor descriptor;
    private final Descriptors.FileDescriptor[] additionalDescriptors;

    public Entity(EntityType entityType, Class entityClass, Descriptors.ServiceDescriptor descriptor, Descriptors.FileDescriptor[] additionalDescriptors) {
        this.entityType = entityType;
        this.entityClass = entityClass;
        this.descriptor = descriptor;
        this.additionalDescriptors = additionalDescriptors;
        Objects.nonNull(entityType);
        Objects.nonNull(entityClass);
        Objects.nonNull(descriptor);
        Objects.nonNull(additionalDescriptors);
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public Class getEntityClass() {
        return entityClass;
    }

    public Descriptors.ServiceDescriptor getDescriptor() {
        return descriptor;
    }

    public Descriptors.FileDescriptor[] getAdditionalDescriptors() {
        return additionalDescriptors;
    }

    @Override
    public String toString() {
        return "Entity{" +
                "entityType=" + entityType +
                ", entityClass=" + entityClass +
                ", descriptor=" + descriptor +
                ", additionalDescriptors=" + Arrays.toString(additionalDescriptors) +
                '}';
    }
}
