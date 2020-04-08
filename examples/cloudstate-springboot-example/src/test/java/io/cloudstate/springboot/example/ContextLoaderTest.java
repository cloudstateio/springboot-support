package io.cloudstate.springboot.example;

import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpEntities;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.concurrent.CompletionStage;

import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class)
public class ContextLoaderTest {
    private static final Logger log = LoggerFactory.getLogger(ContextLoaderTest.class);

    private static final int PROXY_PORT = 9000;
    private static final String FUNCTION_PORT = "8080";

    private static GenericContainer proxy;

    private static ActorSystem system;
    private static Materializer materializer;

    @Autowired
    private RuleService service;

    @Before
    public void setup(){
        system = ActorSystem.create();
        materializer = ActorMaterializer.create(system);

        proxy = new FixedHostPortGenericContainer("cloudstateio/cloudstate-proxy-native-dev-mode:latest")
                .withNetworkMode("host")
                .withExposedPorts(9000)
                .withEnv("USER_FUNCTION_PORT", FUNCTION_PORT)
                .withLogConsumer(new Slf4jLogConsumer(log))
                .waitingFor(
                    Wait.forLogMessage(".*CloudState proxy online.*", 1)
            );

    }

    @Test
    public void load(){
        assertThat(service).isNotNull();
    }

    @Test
    public void gettingShoppingCartReturnOkStatus() throws Exception {
        proxy.start();

        HttpRequest.POST(String.format("http://localhost:%s/cart/1/items/add", PROXY_PORT))
                .withEntity(
                        HttpEntities.create(ContentTypes.APPLICATION_JSON,
                                "{\"productId\": \"foo\", \"name\": \"A foo\", \"quantity\": 20}"));

        final CompletionStage<HttpResponse> responseFuture =
                Http.get(system)
                        .singleRequest(HttpRequest.create(
                                String.format("http://localhost:%s/carts/1/items", PROXY_PORT)), materializer);

        final HttpResponse httpResponse = responseFuture.toCompletableFuture().get();

        assertTrue(httpResponse.status().isSuccess());

    }

}
