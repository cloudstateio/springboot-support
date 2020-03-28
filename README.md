# Spring Boot Cloudstate Starter

# Content

1. [Getting Started](#getting-started)
2. [Configuration](#configuration)
3. [Context Injection](#context-injection)
4. [Running via Cloudstate CLI](#running-via-cloudstate-cli)

## Getting Started
***Note: This getting started is based on the official Cloudstate example from shopping-cart. For more information consult the [official documentation](https://cloudstate.io/docs/).***

First add the dependency of the cloudstater starter to your project. Here an Maven example:

```xml
<dependencies>
    <dependency>
        <groupId>io.cloudstate</groupId>
        <artifactId>cloudstate-springboot-starter</artifactId>
        <version>0.4.3</version>
    </dependency>
</dependencies>
```

Cloudstate applications are based on contracts created via grpc so we must add some more build configurations:

```xml
 <build>

        <extensions>
            <extension>
                <groupId>kr.motd.maven</groupId>
                <artifactId>os-maven-plugin</artifactId>
                <version>1.6.0</version>
            </extension>
        </extensions>

        <plugins>
            <plugin>
                <groupId>org.xolstice.maven.plugins</groupId>
                <artifactId>protobuf-maven-plugin</artifactId>
                <version>0.6.1</version>
                <configuration>
                    <protocExecutable>/usr/local/bin/protoc</protocExecutable>
                    <protocArtifact>com.google.protobuf:protoc:3.9.1:exe:${os.detected.classifier}</protocArtifact>
                </configuration>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>compile</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
</build>
```

Here we have an example of a pom.xml file with all the necessary parts present:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xmlns="http://maven.apache.org/POM/4.0.0"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <artifactId>cloudstate-springboot-example</artifactId>

    <properties>
        <main.class>io.cloudstate.springboot.example.Main</main.class>
    </properties>

    <parent>
        <artifactId>examples</artifactId>
        <groupId>io.cloudstate</groupId>
        <version>0.4.3</version>
    </parent>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-parent</artifactId>
                <version>2.2.4.RELEASE</version>
                <type>pom</type>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>io.cloudstate</groupId>
            <artifactId>cloudstate-springboot-starter</artifactId>
            <version>0.4.3</version>
        </dependency>
    </dependencies>

    <build>
        <extensions>
            <extension>
                <groupId>kr.motd.maven</groupId>
                <artifactId>os-maven-plugin</artifactId>
                <version>1.6.0</version>
            </extension>
        </extensions>

        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>

            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>build-helper-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <phase>generate-sources</phase>
                        <goals>
                            <goal>add-source</goal>
                        </goals>
                        <configuration>
                            <sources>
                                <source>${project.build.directory}/generated-sources/protobuf/java</source>
                            </sources>
                        </configuration>
                    </execution>
                </executions>
            </plugin>

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.5.1</version>
                <configuration>
                    <source>1.8</source>
                    <target>1.8</target>
                </configuration>
            </plugin>

            <plugin>
                <groupId>org.xolstice.maven.plugins</groupId>
                <artifactId>protobuf-maven-plugin</artifactId>
                <version>0.6.1</version>
                <configuration>
                    <protocExecutable>/usr/local/bin/protoc</protocExecutable>
                    <protocArtifact>com.google.protobuf:protoc:3.9.1:exe:${os.detected.classifier}</protocArtifact>
                </configuration>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>compile</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

            <plugin>
                <groupId>com.google.cloud.tools</groupId>
                <artifactId>jib-maven-plugin</artifactId>
                <version>1.7.0</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>build</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <to>
                        <image>sleipnir/cloudstate-boot-example</image>
                        <credHelper></credHelper>
                        <tags>
                            <tag>${project.version}</tag>
                        </tags>
                    </to>
                    <container>
                        <mainClass>${main.class}</mainClass>
                        <jvmFlags>
                            <jvmFlag>-XX:+UseG1GC</jvmFlag>
                            <jvmFlag>-XX:+UseStringDeduplication</jvmFlag>
                        </jvmFlags>
                        <ports>
                            <port>8080</port>
                        </ports>
                    </container>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>
```

Write your Cloudstate function as normal:

```java
/**
 * An event sourced entity.
 */
@EventSourcedEntity
public class ShoppingCartEntity {

    private final String entityId;
    private final Map<String, Shoppingcart.LineItem> cart = new LinkedHashMap<>();

    public ShoppingCartEntity(@EntityId String entityId) {
        this.entityId = entityId;
    }

    @EntityServiceDescriptor
    public static Descriptors.ServiceDescriptor getDescriptor() {
        return Shoppingcart.getDescriptor().findServiceByName("ShoppingCart");
    }

    @EntityAdditionaDescriptors
    public static Descriptors.FileDescriptor[] getAdditionalDescriptors() {
        return new Descriptors.FileDescriptor[]{com.example.shoppingcart.persistence.Domain.getDescriptor()};
    }

    @Snapshot
    public Domain.Cart snapshot() {
        return Domain.Cart.newBuilder()
                .addAllItems(cart.values().stream().map(this::convert).collect(Collectors.toList()))
                .build();
    }

    @SnapshotHandler
    public void handleSnapshot(Domain.Cart cart) {
        this.cart.clear();
        for (Domain.LineItem item : cart.getItemsList()) {
            this.cart.put(item.getProductId(), convert(item));
        }
    }

    @EventHandler
    public void itemAdded(Domain.ItemAdded itemAdded) {
        Shoppingcart.LineItem item = cart.get(itemAdded.getItem().getProductId());
        if (item == null) {
            item = convert(itemAdded.getItem());
        } else {
            item =
                    item.toBuilder()
                            .setQuantity(item.getQuantity() + itemAdded.getItem().getQuantity())
                            .build();
        }
        cart.put(item.getProductId(), item);
    }

    @EventHandler
    public void itemRemoved(Domain.ItemRemoved itemRemoved) {
        cart.remove(itemRemoved.getProductId());
    }

    @CommandHandler
    public Shoppingcart.Cart getCart() {
        return Shoppingcart.Cart.newBuilder().addAllItems(cart.values()).build();
    }

    @CommandHandler
    public Empty addItem(Shoppingcart.AddLineItem item, CommandContext ctx) {
        if (item.getQuantity() <= 0) {
            ctx.fail("Cannot add negative quantity of to item" + item.getProductId());
        }
        ctx.emit(
                Domain.ItemAdded.newBuilder()
                        .setItem(
                                Domain.LineItem.newBuilder()
                                        .setProductId(item.getProductId())
                                        .setName(item.getName())
                                        .setQuantity(item.getQuantity())
                                        .build())
                        .build());
        return Empty.getDefaultInstance();
    }

    @CommandHandler
    public Empty removeItem(Shoppingcart.RemoveLineItem item, CommandContext ctx) {
        if (!cart.containsKey(item.getProductId())) {
            ctx.fail("Cannot remove item " + item.getProductId() + " because it is not in the cart.");
        }
        ctx.emit(Domain.ItemRemoved.newBuilder().setProductId(item.getProductId()).build());
        return Empty.getDefaultInstance();
    }

    private Shoppingcart.LineItem convert(Domain.LineItem item) {
        return Shoppingcart.LineItem.newBuilder()
                .setProductId(item.getProductId())
                .setName(item.getName())
                .setQuantity(item.getQuantity())
                .build();
    }

    private Domain.LineItem convert(Shoppingcart.LineItem item) {
        return Domain.LineItem.newBuilder()
                .setProductId(item.getProductId())
                .setName(item.getName())
                .setQuantity(item.getQuantity())
                .build();
    }
}

```

The only thing other than a normal cloudstate function is the need to register the descriptors explicitly:

```java
    @EntityServiceDescriptor
    public static Descriptors.ServiceDescriptor getDescriptor() {
        return Shoppingcart.getDescriptor().findServiceByName("ShoppingCart");
    }

    @EntityAdditionaDescriptors
    public static Descriptors.FileDescriptor[] getAdditionalDescriptors() {
        return new Descriptors.FileDescriptor[]{com.example.shoppingcart.persistence.Domain.getDescriptor()};
    }
```
***These registration annotations can be used for any type (CRDT's, EventSourced) of Cloudstate function.***


Then write your main class in the Spring boot style. 
Uses the **@EnableCloudstate** annotation to tell Spring what to do:

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.cloudstate.springboot.starter.autoconfigure.EnableCloudstate;

@EnableCloudstate
@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}

```
Run the application in the same way as you would any other springboot application:

```shell script
[sleipnir@cloudstate cloudstate-springboot-example]# mvn spring-boot:run
[INFO] Scanning for projects...


  ______  __        ______    __    __   _______       _______.___________.    ___   .___________. _______
 /      ||  |      /  __  \  |  |  |  | |       \     /       |           |   /   \  |           ||   ____|
|  ,----'|  |     |  |  |  | |  |  |  | |  .--.  |   |   (----`---|  |----`  /  ^  \ `---|  |----`|  |__
|  |     |  |     |  |  |  | |  |  |  | |  |  |  |    \   \       |  |      /  /_\  \    |  |     |   __|
|  `----.|  `----.|  `--'  | |  `--'  | |  '--'  |.----)   |      |  |     /  _____  \   |  |     |  |____
 \______||_______| \______/   \______/  |_______/ |_______/       |__|    /__/     \__\  |__|     |_______|


Cloudtate v0.4.3
2020-03-26 17:58:43.970  INFO 6286 --- [           main] io.cloudstate.springboot.example.Main    : Starting Main on sleipnir with PID 6286 (/home/sleipnir/development/workspace/pessoal/cloudstate-repos/spring-boot-cloudstate-starter/examples/cloudstate-springboot-example/target/classes started by root in /home/sleipnir/development/workspace/pessoal/cloudstate-repos/spring-boot-cloudstate-starter)
2020-03-26 17:58:43.974 DEBUG 6286 --- [           main] io.cloudstate.springboot.example.Main    : Running with Spring Boot v2.2.4.RELEASE, Spring v5.2.3.RELEASE
2020-03-26 17:58:43.975  INFO 6286 --- [           main] io.cloudstate.springboot.example.Main    : No active profile set, falling back to default profiles: default
2020-03-26 17:58:45.817 DEBUG 6286 --- [           main] i.c.s.s.i.scan.CloudstateEntityScan      : Registering Entity -> Entity{entityType=EventSourced, entityClass=class io.cloudstate.springboot.example.ShoppingCartEntity, descriptor=com.google.protobuf.Descriptors$ServiceDescriptor@58516c91, additionalDescriptors=[com.google.protobuf.Descriptors$FileDescriptor@7c129ef6]}
2020-03-26 17:58:45.819 DEBUG 6286 --- [           main] i.c.s.s.i.scan.CloudstateEntityScan      : Entities found in PT1.072779S
2020-03-26 17:58:46.078  INFO 6286 --- [           main] i.c.s.s.a.CloudstateBeanInitialization   : Starting Cloudstate Server...
[DEBUG] [03/26/2020 17:58:46.345] [main] [EventStream(akka://StatefulService)] logger log1-Logging$DefaultLogger started
[DEBUG] [03/26/2020 17:58:46.346] [main] [EventStream(akka://StatefulService)] Default Loggers started
[DEBUG] [03/26/2020 17:58:46.414] [main] [AkkaSSLConfig(akka://StatefulService)] Initializing AkkaSSLConfig extension...
[DEBUG] [03/26/2020 17:58:46.415] [main] [AkkaSSLConfig(akka://StatefulService)] buildHostnameVerifier: created hostname verifier: com.typesafe.sslconfig.ssl.DefaultHostnameVerifier@1989e8c6
[DEBUG] [03/26/2020 17:58:46.723] [main] [akka.actor.ActorSystemImpl(StatefulService)] Binding server using HTTP/2
2020-03-26 17:58:46.841  INFO 6286 --- [           main] io.cloudstate.springboot.example.Main    : Started Main in 3.255 seconds (JVM running for 3.671)
[DEBUG] [03/26/2020 17:58:46.847] [StatefulService-akka.actor.default-dispatcher-4] [akka://StatefulService/system/IO-TCP/selectors/$a/0] Successfully bound to /0:0:0:0:0:0:0:0:8080


```

The complete source code can be found [here](/examples/cloudstate-springboot-example).

## Configuration

It is possible to configure certain application parameters either via the conventional springboot configuration file 
( **application.properties** or **application.yaml**) or via the HOCON configuration file (**application.conf**).

See some examples:

***application.yml***
```yaml
logging:
  level:
    root: ERROR
    org.springframework: INFO
    io.cloudstate.springboot: DEBUG

io:
  cloudstate:
    user-function-interface: "localhost"
    user-function-port: 8080

```

***application.conf:***
```json
cloudstate {

  user-function-interface = "0.0.0.0"
  user-function-interface = ${?HOST}

  user-function-port = 8080
  user-function-port = ${?PORT}


  system {
    akka {
      loglevel = "DEBUG"
    }

    loggers = ["akka.event.slf4j.Slf4jLogger"]
    logging-filter = "akka.event.slf4j.Slf4jLoggingFilter"
  }
}
```

## Context Injection

Comming soon

## Running via Cloudstate CLI

Comming soon