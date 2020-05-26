package io.cloudstate.springboot.starter.internal;

import io.cloudstate.springboot.starter.autoconfigure.CloudstateAutoConfiguration;
import io.cloudstate.springboot.starter.autoconfigure.CloudstateProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes= CloudstateAutoConfiguration.class)
public class CloudstateEntityScanTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private CloudstateProperties cloudstateProperties;

    @Test
    public void findEntities_NotBeEmpty(){
        CloudstateEntityScan scan = new CloudstateEntityScan(context, cloudstateProperties);
        final List<Entity> entities = scan.findEntities();

        assertNotNull(entities);
        assertFalse(entities.isEmpty());
        assertEquals(2, entities.size());
    }
}