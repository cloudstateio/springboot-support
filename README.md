# Spring Boot Cloudstate Starter

# Content

1. [Getting Started](#getting-started)
2. [Configuration](#configuration)
3. [Advanced Configuration](#advanced-configuration)
4. [Context Injection](#context-injection)
5. [Conventions and Restrictions](#conventions-and-restrictions)
6. [Forwarding and effects](#forwarding-and-effects)
7. [Running via Cloudstate CLI](#running-via-cloudstate-cli)

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

Write your Cloudstate function:

```java
/**
 * An event sourced entity.
 */
@EventSourcedEntity
@CloudstateEntityBean
public class ShoppingCartEntity {
    private final Map<String, Shoppingcart.LineItem> cart = new LinkedHashMap<>();

    @EntityId
    private String entityId;

    @CloudstateContext
    private EventSourcedContext context;

    @Autowired
    private RuleService ruleService;

    @Autowired
    private ShoppingCartTypeConverter typeConverter;

    @Snapshot
    public Domain.Cart snapshot() {
        return Domain.Cart.newBuilder()
                .addAllItems(cart.values().stream().map(typeConverter::convert).collect(Collectors.toList()))
                .build();
    }

    @SnapshotHandler
    public void handleSnapshot(Domain.Cart cart) {
        this.cart.clear();
        for (Domain.LineItem item : cart.getItemsList()) {
            this.cart.put(item.getProductId(), typeConverter.convert(item));
        }
    }

    @EventHandler
    public void itemAdded(Domain.ItemAdded itemAdded) {
        Shoppingcart.LineItem item = cart.get(itemAdded.getItem().getProductId());
        if (item == null) {
            item = typeConverter.convert(itemAdded.getItem());
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
        if (!ruleService.isValidAmount(item)) {
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
}

```
***Don't worry about the details, we'll explain everything later.***

To work Cloudstate requires that the descriptors of the protobuf's files are explicitly registered.
We have two ways to do this one is:

* Via Springboot,  by creating a Spring Configuration class and registering these types accordingly. 
* And the other way we’ll explain later in Conventions and Restrictions.

Here is an example of a suitable configuration class:

```java
import com.example.shoppingcart.Shoppingcart;
import com.google.protobuf.Descriptors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DescriptorsConfiguration {

    @Bean
    public Descriptors.ServiceDescriptor shoppingCartEntityServiceDescriptor() {
        return Shoppingcart.getDescriptor().findServiceByName("ShoppingCart");
    }

    @Bean
    public Descriptors.FileDescriptor[] shoppingCartEntityFileDescriptors() {
        return new Descriptors.FileDescriptor[]{com.example.shoppingcart.persistence.Domain.getDescriptor()};
    }
}
```

Then write your main class in the Spring boot style. 
Uses the **@EnableCloudstate** annotation to tell Spring what to do:

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.cloudstate.springboot.starter.EnableCloudstate;

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
2020-04-03 14:42:52.090  INFO 14233 --- [           main] io.cloudstate.springboot.example.Main    : Starting Main on sleipnir with PID 14233 (/home/sleipnir/development/workspace/pessoal/cloudstate-repos/spring-boot-cloudstate-starter/examples/cloudstate-springboot-example/target/classes started by root in /home/sleipnir/development/workspace/pessoal/cloudstate-repos/spring-boot-cloudstate-starter)
2020-04-03 14:42:52.095  INFO 14233 --- [           main] io.cloudstate.springboot.example.Main    : No active profile set, falling back to default profiles: default
2020-04-03 14:42:53.120  INFO 14233 --- [           main] i.c.s.s.a.CloudstateBeanInitialization   : Starting Cloudstate Server...
2020-04-03 14:42:53.960  INFO 14233 --- [           main] io.cloudstate.springboot.example.Main    : Started Main in 2.261 seconds (JVM running for 2.623)

```

Or via docker after build:

```shell script
[sleipnir@sleipnir spring-boot-cloudstate-starter]# docker run --rm --name shoppingcart-spring --net=host sleipnir/cloudstate-boot-example:0.4.3
Unable to find image 'sleipnir/cloudstate-boot-example:0.4.3' locally
0.4.3: Pulling from sleipnir/cloudstate-boot-example
aad63a933944: Already exists 
7e9e08010be5: Pull complete 
125def8f4f2c: Pull complete 
c2fe10915d96: Pull complete 
70b87afe104e: Pull complete 
2f2e42ac6971: Pull complete 
12be80d74a84: Pull complete 
46aa73b3d13b: Pull complete 
Digest: sha256:ed66f472ab0875f0ec25ac5f953fb9e9197cb5e5a9211826630a4853e1cde64d
Status: Downloaded newer image for sleipnir/cloudstate-boot-example:0.4.3
SLF4J: Class path contains multiple SLF4J bindings.
SLF4J: Found binding in [jar:file:/app/libs/logback-classic-1.2.3.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:/app/libs/slf4j-simple-1.7.26.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
SLF4J: Actual binding is of type [ch.qos.logback.classic.util.ContextSelectorStaticBinder]


  ______  __        ______    __    __   _______       _______.___________.    ___   .___________. _______
 /      ||  |      /  __  \  |  |  |  | |       \     /       |           |   /   \  |           ||   ____|
|  ,----'|  |     |  |  |  | |  |  |  | |  .--.  |   |   (----`---|  |----`  /  ^  \ `---|  |----`|  |__
|  |     |  |     |  |  |  | |  |  |  | |  |  |  |    \   \       |  |      /  /_\  \    |  |     |   __|
|  `----.|  `----.|  `--'  | |  `--'  | |  '--'  |.----)   |      |  |     /  _____  \   |  |     |  |____
 \______||_______| \______/   \______/  |_______/ |_______/       |__|    /__/     \__\  |__|     |_______|


Cloudtate v0.4.3
2020-04-07 16:05:48.371  INFO 1 --- [           main] io.cloudstate.springboot.example.Main    : Starting Main on b5ba1455c3c9 with PID 1 (/app/classes started by root in /)
2020-04-07 16:05:48.373  INFO 1 --- [           main] io.cloudstate.springboot.example.Main    : No active profile set, falling back to default profiles: default
2020-04-07 16:05:48.705  INFO 1 --- [           main] trationDelegate$BeanPostProcessorChecker : Bean 'io.cloudstate-io.cloudstate.springboot.starter.autoconfigure.CloudstateProperties' of type [io.cloudstate.springboot.starter.autoconfigure.CloudstateProperties] is not eligible for getting processed by all BeanPostProcessors (for example: not eligible for auto-proxying)
2020-04-07 16:05:48.706  INFO 1 --- [           main] trationDelegate$BeanPostProcessorChecker : Bean 'io.cloudstate.springboot.starter.autoconfigure.CloudstateAutoConfiguration' of type [io.cloudstate.springboot.starter.autoconfigure.CloudstateAutoConfiguration$$EnhancerBySpringCGLIB$$212a4b21] is not eligible for getting processed by all BeanPostProcessors (for example: not eligible for auto-proxying)
2020-04-07 16:05:48.720  INFO 1 --- [           main] trationDelegate$BeanPostProcessorChecker : Bean 'cloudstateEntityScan' of type [io.cloudstate.springboot.starter.internal.scan.CloudstateEntityScan] is not eligible for getting processed by all BeanPostProcessors (for example: not eligible for auto-proxying)
2020-04-07 16:05:48.722  INFO 1 --- [           main] trationDelegate$BeanPostProcessorChecker : Bean 'stateController' of type [java.lang.ThreadLocal$SuppliedThreadLocal] is not eligible for getting processed by all BeanPostProcessors (for example: not eligible for auto-proxying)
2020-04-07 16:05:48.935  INFO 1 --- [  cloudstate-t1] i.c.s.s.i.CloudstateBeanInitialization   : Starting Cloudstate Server...
2020-04-07 16:05:48.943  INFO 1 --- [           main] io.cloudstate.springboot.example.Main    : Started Main in 0.89 seconds (JVM running for 1.193)


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
    user-function-port: 8080
    user-function-interface: "localhost"
    user-function-package-name: "io.cloudstate.springboot.example"

```
***Cloudstate Springboot Support uses Classpath scanning to assist in the registration step of entity functions, 
you can explicitly specify in which package the system should look for its entities. 
This can speed up the application's bootstrap by more than a second in most cases.
To define your package use the user-function-package-name property as done in the example above. 
This property is only available for spring configuration files (application.yml or application.properties).***


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

## Advanced Configuration

Certain characteristics of the application startup can be regulated using two other configuration parameters:

* **AutoRegister**: Default ***true***. It establishes that the entities must be registered automatically by the Spring container during its initialization.
* **AutoStartup**: Default ***true***. Establishes that the Cloudstate server should be started automatically.

Spring application.yml:

```yaml
io:
  cloudstate:
    auto-register: true
    auto-startup: true
```

In case these two parameters are set to false then you must register and boot the application manually. Here is an example:

```java
@Component
public class ManualStartupProcess {
    
    @Autowired
    private ShoppingCartEntity entity;
    
    @Autowired
    private RegistrarService registrarService;
    
    @PostConstruct
    public void boot() throws Exception {
        //Register entity
        Cloudstate cloudstate = registrarService.register(entity);
    
        // Start the service
        cloudstate.start()
            .toCompletableFuture()
            .exceptionally(ex -> {
                log.error("Failure on Cloudstate Server startup", ex);
                return Done.done();
            })
            .thenAccept(done -> {
                log.info("Cloudstate Server start successfully");
            });
    }

} 
```

In the example above we are using an auxiliary service class called RegistrarService to register an entity managed by Spring. 
However, you may want to register an entity class without any special annotations and that is not managed by Spring. 
In this case you will have to register your entity in the same way as if you were using the Cloudstate Java Support library directly:

```java
@Component
public class ManualStartupProcess {
    
    @Autowired
    private Cloudstate cloudstate;
    
    @PostConstruct
    public void boot() throws Exception {
    
        // Register and start the service
        cloudstate.registerEventSourcedEntity(
                ShoppingCartEntity.class,
                Shoppingcart.getDescriptor().findServiceByName("ShoppingCart"))
            .start()
            .toCompletableFuture()
            .exceptionally(ex -> {
                log.error("Failure on Cloudstate Server startup", ex);
                return Done.done();
            })
            .thenAccept(done -> {
                log.info("Cloudstate Server start successfully");
            });
    }

} 
```

But, although available, **we discourage the use of these APIs for direct use**, as this library takes care of all these steps 
automatically for you without any effort on your part.

## Context Injection

As we saw in the Getting Started example, it is perfectly possible to inject any Bean available in Spring into a 
Cloudstate entity class.
It is also possible to use the annotations present in the javax.inject package 
([JSR330](https://jcp.org/en/jsr/detail?id=330)). 
The Cloudstate Springboot Support library already includes the necessary dependencies so you don't have to worry about it.

***At the moment we can only inject dependencies via class properties. 
In Conventions and Restrictions we explain the reasons why.***

You can annotate your entity classes with Spring ***@Component*** or ***@Service*** annotations but we have created a convenient 
annotation that we call ***@CloudstateEntityBean*** that can be used for that too.

## Conventions and Restrictions

Both Springboot and Cloudstate have some conventions and / or restrictions for creating objects. 
In the sections below we describe those that are most relevant

### Registering Protobuf Descriptors
As mentioned earlier, there are two ways to register the protobuf descriptor files and here we will explain each one in 
detail.

First using the Spring configuration.
 
Spring's bean declaration conventions define that the method name is exactly the name that will be registered in
the Spring injection container as a qualifier. 
So if you use method names other than those defined in the Cloudstate Springboot support convention:

 (entity.getSimpleName() + "ServiceDescriptor" for example)
 
***Remembering that the first letter must always be lowercase, as well as the method and variable naming convention in Java***

Then you will need to use the name property of the '**@Bean**' annotation and define the name following these conventions.

If your entity class is called ShoppinCartEntity then you can declare the beans as below:

```java

@Bean(name = "shoppingCartEntityServiceDescriptor")
public Descriptors.ServiceDescriptor serviceDescriptor() {
    return Shoppingcart.getDescriptor().findServiceByName("ShoppingCart");
}

@Bean(name = "shoppingCartEntityFileDescriptors")
public Descriptors.FileDescriptor[] fileDescriptors() {
   return new Descriptors.FileDescriptor[] {com.example.shoppingcart.persistence.Domain.getDescriptor()};
}
```

Or like this:

```java
@Bean
public Descriptors.ServiceDescriptor shoppingCartEntityServiceDescriptor() {
   return Shoppingcart.getDescriptor().findServiceByName("ShoppingCart");
}

@Bean
public Descriptors.FileDescriptor[] shoppingCartEntityFileDescriptors() {
    return new Descriptors.FileDescriptor[] {com.example.shoppingcart.persistence.Domain.getDescriptor()};
}
```

The other way is to use the created entity class itself and declare some annotated static methods like the example below:

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

We prefer that you adopt the version based on the Spring conventions using configuration classes as in the 
Getting Started example.

### JSR330

The Cloudastate Springboot Support library supports JSR330 within the scope of the support provided by Spring itself to 
this specification.
Note that the Cloudstate Java Support library on which we depend allows you to bind Cloudstate and any other DI container 
you want. However, no specific module for any of these other containers has yet been made.

Feel free to contribute or suggest support for more runtimes.

### Injecting EntityId and Cloudstate Context Objects

You can use the @EntityId annotation to access the managed entity's id.
It is also possible to have access to the EventSourcedEntityCreationContext created during the activation of the object 
by Cloudstate. However for this you will need to annotate the Context property with the annotation @CloudstateContext 
as in the example below:

```
@EntityId
private String entityId;

@CloudstateContext
private EventSourcedContext context;
```

### Using properties instead constructors

Unfortunately in this present version of the library we do not support injection via constructors. 
We know that this is not a good practice mainly for creating tests, but due to some characteristics 
of the life cycle of objects managed by cloudstate java support, we are currently unable to provide support to constructors.
This is not to say that it will always be so and we hope to resolve these 
[issue](https://github.com/sleipnir/spring-boot-cloudstate-starter/issues/6) soon and enable the use of constructors 
in the future.

Obviously this is only a problem if you want to inject EntityId or EventSourcedEntityCreationContext. 
Otherwise, if you want to inject only other Beans from the Spring Context you can use injection via constructors as normal.

The constructors below would be perfectly acceptable:

```java
@EventSourcedEntity
@CloudstateEntityBean
public final class ShoppingCartEntity {
    private final Map<String, Shoppingcart.LineItem> cart = new LinkedHashMap<>();

    @EntityId
    private String entityId;

    @CloudstateContext
    private EventSourcedContext context;

    private final RuleService ruleService;

    private final ShoppingCartTypeConverter typeConverter;
    
    @Autowired
    public ShoppingCartEntity(RuleService ruleService, ShoppingCartTypeConverter typeConverter) {
        this.ruleService = ruleService;
        this.typeConverter = typeConverter;
    }
    
    //......
}
```
***As you can see, the constructor injection constraint applies only to EntityId and CreationContext. 
So, as in the example above, you can mix the approaches and get the best of both worlds together***

## Forwarding and effects

This page documents how to use Cloudstate CRDT effects and forwarding in Java Springboot manner. For high level information on what 
Cloudstate effects and forwarding is, please read the general 
[Forwarding and effects](https://cloudstate.io/docs/user/features/effects.html) documentation first.
Looking up service call references

To forward a command or emit an effect, a reference to the service call that will be invoked needs to be looked up. 
This can be done using the ServiceCallFactory interface, which is accessible on any context object 
via the serviceCallFactory() method.

For example, if a user function serves two entity types, a shopping cart, and a CRDT that tracks which items 
are currently in hot demand, it might want to invoke the ItemAddedToCart command on example.shoppingcart.HotItems 
as a side effect of the AddItem shopping cart command. This reference can be looked up like so:

```java
private static Logger log = LoggerFactory.getLogger(ShoppingCartEntity.class);

private final ServiceCallRef<Hotitems.Item> itemAddedToCartRef;

@EntityId
private String entityId;

@CloudstateContext
private EventSourcedContext context;

@PostConstruct
public void setup() {
    log.info(
            "Setup ShoppingCartEntity with EntityId: {}. And EventSourcedContext: {}",
            entityId, context);

    itemAddedToCartRef =
          ctx.serviceCallFactory()
              .lookup(
                  "io.cloudstate.springboot.example.ShoppingCartService", "ItemAddedToCart", Hotitems.Item.class);
}

```
This could be looked up in the @PostConstruct annotated method of the entity, for later use, so it doesn’t have to be looked up each time it’s needed.

## Running via Cloudstate CLI

A template to use the Cloudstate CLI is being created and soon it will be possible to create a 'scaffold' of this project via CLI.
It is clear that for this to happen it is necessary that the [PR](https://github.com/cloudstateio/cloudstate/pull/227) 
with the proposal of the mechanism that allows 
this implementation to work is accepted by the Cloudstate team.


***Have fun :)***