package io.cloudstate.springboot.starter.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "io.cloudstate")
public class CloudstateProperties {

    private Map<String, List<String>> eventSourcedDescriptors = new HashMap<>();
    private Map<String, List<String>> crdtDescriptors = new HashMap<>();

    public Map<String, List<String>> getEventSourcedDescriptors() {
        return eventSourcedDescriptors;
    }

    public Map<String, List<String>> getCrdtDescriptors() {
        return crdtDescriptors;
    }
}
