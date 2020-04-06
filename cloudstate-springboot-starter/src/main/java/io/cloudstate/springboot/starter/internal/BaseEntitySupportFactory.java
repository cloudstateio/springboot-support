package io.cloudstate.springboot.starter.internal;

import io.cloudstate.javasupport.Context;
import io.cloudstate.javasupport.EntitySupportFactory;
import io.cloudstate.springboot.starter.internal.scan.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Map;

import static io.cloudstate.springboot.starter.internal.CloudstateUtils.postConstructObject;

class BaseEntitySupportFactory implements EntitySupportFactory {
    private static final Logger LOG = LoggerFactory.getLogger(CloudstateUtils.class);

    private final Entity entity;
    private final ApplicationContext context;
    private final ThreadLocal<Map<Class<?>, Map<String, Object>>> stateController;

    public BaseEntitySupportFactory(Entity entity, ApplicationContext context, ThreadLocal<Map<Class<?>, Map<String, Object>>> stateController) {
        this.entity = entity;
        this.context = context;
        this.stateController = stateController;
    }

    @Override
    public Object create(Context eventSourcedEntityCreationContext, String entityId) {
        LOG.trace("Create instance of EventSourcedEntity");
        postConstructObject(stateController, entity.getEntityClass(), eventSourcedEntityCreationContext, entityId);
        return context.getBean(entity.getEntityClass());
    }

    @Override
    public Class<?> typeClass() {
        return entity.getEntityClass();
    }

}