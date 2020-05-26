package io.cloudstate.springboot.starter;

import io.cloudstate.javasupport.CloudState;
import io.cloudstate.springboot.starter.autoconfigure.CloudstateAutoConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static junit.framework.TestCase.assertNotNull;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CloudstateAutoConfiguration.class)
public class ContextLoaderTest {

    @Autowired
    private CloudState cloudState;

    @Test
    public void cloudstate_NotBeNull(){
        assertNotNull(cloudState);
    }
}
