package io.cloudstate.springboot.starter;

import io.cloudstate.springboot.starter.autoconfigure.CloudstateProperties;

public final class CloudstateEntityScan {

    private final CloudstateProperties properties;

    public CloudstateEntityScan(final CloudstateProperties properties) {
        this.properties = properties;
    }
}

