package io.cloudstate.springboot.starter.internal;

import io.cloudstate.javasupport.Context;
import io.cloudstate.javasupport.EntitySupportFactory;
import io.cloudstate.springboot.starter.internal.scan.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import static io.cloudstate.springboot.starter.internal.CloudstateUtils.postConstructObject;

class BaseEntitySupportFactory implements EntitySupportFactory {
    private static final Logger LOG = LoggerFactory.getLogger(CloudstateUtils.class);

    private final Entity entity;
    private final ApplicationContext context;

    public BaseEntitySupportFactory(Entity entity, ApplicationContext context) {
        this.entity = entity;
        this.context = context;
    }

    @Override
    public Object create(Context eventSourcedEntityCreationContext, String entityId) {
        LOG.trace("Create instance of EventSourcedEntity");
        Object obj = context.getBean(entity.getEntityClass());
        return postConstructObject(obj, eventSourcedEntityCreationContext, entityId);
    }

    @Override
    public Class<?> typeClass() {
        return entity.getEntityClass();
    }

}
