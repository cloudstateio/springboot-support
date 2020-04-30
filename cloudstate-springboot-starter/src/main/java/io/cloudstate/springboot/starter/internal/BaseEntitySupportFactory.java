package io.cloudstate.springboot.starter.internal;

import io.cloudstate.javasupport.EntityContext;
import io.cloudstate.javasupport.EntityFactory;
import io.cloudstate.springboot.starter.internal.scan.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import static io.cloudstate.springboot.starter.internal.CloudstateUtils.postConstructObject;

class BaseEntitySupportFactory implements EntityFactory {
    private static final Logger LOG = LoggerFactory.getLogger(CloudstateUtils.class);

    private final Entity entity;
    private final ApplicationContext context;

    public BaseEntitySupportFactory(Entity entity, ApplicationContext context) {
        this.entity = entity;
        this.context = context;
    }

    @Override
    public Object create(EntityContext entityContext) {
        LOG.trace("Create instance of EventSourcedEntity");
        Object obj = context.getBean(entity.getEntityClass());
        return postConstructObject(obj, entityContext, entityContext.entityId());
    }

    @Override
    public Class<?> entityClass() {
        return entity.getEntityClass();
    }
}
