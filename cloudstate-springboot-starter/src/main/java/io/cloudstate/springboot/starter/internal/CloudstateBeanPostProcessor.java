package io.cloudstate.springboot.starter.internal;

import io.cloudstate.springboot.starter.internal.scan.CloudstateEntityScan;
import io.cloudstate.springboot.starter.internal.scan.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

@Component
public final class CloudstateBeanPostProcessor implements BeanPostProcessor {
    private static Logger log = LoggerFactory.getLogger(CloudstateBeanPostProcessor.class);

    private final CloudstateEntityScan entityScan;
    private final ThreadLocal<Map<Class<?>, Map<String, Object>>> stateController;

    @Autowired
    public CloudstateBeanPostProcessor(
            CloudstateEntityScan entityScan, ThreadLocal<Map<Class<?>, Map<String, Object>>> stateController) {
        this.entityScan = entityScan;
        this.stateController = stateController;
    }

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        final Map<Class<?>, Map<String, Object>> stateMap = stateController.get();

        if (stateMap.containsKey(bean.getClass())) {
            Entity e = getEntity(bean);

            log.debug("Register bean definition to {}", e.getEntityClass());
            final Map<String, Object> state = stateMap.get(e.getEntityClass());

            state.entrySet().forEach(entry -> {
                setProperties(bean, e, entry);
            });

        }
        return bean;
    }

    private void setProperties(Object bean, Entity e, Map.Entry<String, Object> entry) {
        log.debug("Bean definition add property: {} with value: {}", entry.getKey(), entry.getValue());
        Field f = null;
        try {
            log.debug("Searching Field: {}", entry.getKey());
            f = e.getEntityClass().getDeclaredField(entry.getKey());
            if (Objects.nonNull(e)) {
                log.debug("Field found: {}. Type: {}", entry.getKey(), entry.getValue());
                f.setAccessible(true);
                f.set(bean, entry.getValue());
            }
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }

    private Entity getEntity(Object bean) {
        return entityScan.findEntities()
                .stream()
                .filter(isElegible(bean))
                .findFirst()
                .get();
    }

    private Predicate<Entity> isElegible(Object bean) {
        return entity -> entity.getEntityClass() == bean.getClass();
    }

}
