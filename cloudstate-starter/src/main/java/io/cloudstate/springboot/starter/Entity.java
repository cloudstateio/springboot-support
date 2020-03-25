package io.cloudstate.springboot.starter;

import com.google.protobuf.Descriptors;

import java.util.List;

public final class Entity {

    private final EntityType entityType;
    private final Descriptors.ServiceDescriptor descriptor;
    private final List<Descriptors.FileDescriptor> additionalDescriptors;

    public Entity(EntityType entityType, String descriptor, List<String> additionalDescriptors) {
        this.entityType = entityType;
        this.descriptor = getServiceDescriptor(descriptor);
        this.additionalDescriptors = getAdditionalFileDescriptor(additionalDescriptors);
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public Descriptors.ServiceDescriptor getDescriptor() {
        return descriptor;
    }

    public List<Descriptors.FileDescriptor> getAdditionalDescriptors() {
        return additionalDescriptors;
    }

    public Descriptors.ServiceDescriptor getServiceDescriptor(String qualifiedNameOfDescriptor) {
        return null;
    }

    public List<Descriptors.FileDescriptor> getAdditionalFileDescriptor(List<String> additionalDescriptors) {
        return null;
    }

}
