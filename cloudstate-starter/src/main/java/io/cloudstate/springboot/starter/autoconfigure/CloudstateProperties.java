package io.cloudstate.springboot.starter.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "io.cloudstate")
public class CloudstateProperties {

    private Map<String, String> additionalDescriptors = new HashMap<String, String>();

    public Map<String, String> getAdditionalDescriptors() {
        return additionalDescriptors;
    }
}
