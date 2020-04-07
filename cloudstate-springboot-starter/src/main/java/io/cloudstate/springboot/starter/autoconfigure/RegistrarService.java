package io.cloudstate.springboot.starter.autoconfigure;

import io.cloudstate.javasupport.CloudState;
import io.cloudstate.springboot.starter.internal.CloudstateUtils;
import io.cloudstate.springboot.starter.internal.scan.CloudstateEntityScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RegistrarService {
    private final CloudState cloudState;
    private final ApplicationContext context;
    private final CloudstateEntityScan entityScan;
    private final CloudstateProperties properties;
    private final ThreadLocal<Map<Class<?>, Map<String, Object>>> stateController;

    @Autowired
    public RegistrarService(
            CloudState cloudState,
            ApplicationContext context,
            CloudstateEntityScan entityScan,
            CloudstateProperties properties,
            ThreadLocal<Map<Class<?>, Map<String, Object>>> stateController) {
        this.cloudState = cloudState;
        this.context = context;
        this.entityScan = entityScan;
        this.properties = properties;
        this.stateController = stateController;
    }

    public CloudState registerAllEntities() throws Exception {
        return CloudstateUtils.register(cloudState, stateController, context, entityScan, properties);
    }

    public CloudState register(Object entityInstance) throws Exception {
        return CloudstateUtils.register(entityInstance, cloudState, stateController, context, entityScan, properties);
    }
}
