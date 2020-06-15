# Getting started with Spring Boot

## Prerequisites

## Spring Boot version
Cloudstate Spring Boot support requires Spring Boot >= $cloudstate.springboot.version$.

## Build configurations

Maven 
: @@@vars
```text
<dependencies>
    <dependency>
        <groupId>io.cloudstate</groupId>
        <artifactId>cloudstate-springboot-support</artifactId>
        <version>$cloudstate.springboot.lib.version$</version>
    </dependency>
</dependencies>
```
@@@

sbt 
: @@@vars
```text
libraryDependencies += "io.cloudstate" % "cloudstate-springboot-support" % "$cloudstate.springboot.lib.version$"
```
@@@

gradle 
: @@@vars
```text
compile group: 'io.cloudstate', name: 'cloudstate-springboot-support', version: '$cloudstate.springboot.lib.version$'
```
@@@

Cloudstate applications are based on contracts created via grpc, in the case of Maven-based Spring Boot applications you can use the following plugins to assist in this task:

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
    <groupId>io.cloudstate</groupId>
    <artifactId>cloudstate-springboot-example</artifactId>
    <version>$cloudstate.springboot.lib.version$</version>

    <properties>
        <main.class>io.cloudstate.springboot.example.Main</main.class>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-parent</artifactId>
                <version>$cloudstate.springboot.version$</version>
                <type>pom</type>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>io.cloudstate</groupId>
            <artifactId>cloudstate-springboot-support</artifactId>
            <version>$cloudstate.springboot.lib.version$</version>
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
                    <from>
                        <image>adoptopenjdk/openjdk8-openj9:alpine-slim</image>
                        <credHelper></credHelper>
                    </from>
                    <to>
                        <image>cloudstateio/samples-springboot-shopping-cart</image>
                        <credHelper></credHelper>
                        <tags>
                            <tag>${project.version}</tag>
                        </tags>
                    </to>
                    <container>
                        <mainClass>${main.class}</mainClass>
                        <jvmFlags>
                            <jvmFlag>-XshareClasses</jvmFlag>
                            <jvmFlag>-Xquickstart</jvmFlag>
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

@@@ note { title=Important }
Remember to change the values of the main.class, repo.name, and version tags to their respective values
@@@

Subsequent source locations and build commands will assume the above Maven project, and may need to be adapted to your particular build tool and setup.

## Protobuf files

The Xolstice Maven plugin assumes a location of `src/main/proto` for your protobuf files. In addition, it includes any protobuf files from your application dependencies in the protoc include path, so there's nothing you need to do to pull in either the Cloudstate protobuf types, or any of the Google standard protobuf types, they are all automatically available for import.

So, if you were to build the example shopping cart application shown earlier in @extref[gRPC descriptors](cloudstate:user/features/grpc.html), you could simply paste that protobuf into `src/main/proto/shoppingcart.proto`. You may wish to also define the java package, to ensure the package name used conforms to Java package naming conventions:

```proto
option java_package = "com.example.shoppingcart";
```

Now if you run `mvn compile`, you'll find your generated protobuf files in `target/generated-sources/protobuf/java`.

## Write your Cloudstate function:

In general you will write your entity functions in the usual way as you would if you were using the version of java support. For more information on how to create entity functions, see the specific documentation @extref:[here](cloudstate:user/lang/java/eventsourced.html#event-sourcing), and @extref:[here](cloudstate:user/lang/java/crdt.html#conflict-free-replicated-data-types).

The differences will be explained later in this documentation. Below is an example of an EventSourced user entity:

@@snip [ShoppingCartEntity.java]($base$/docs/src/tests/paradox/ShoppingCartEntity.java) { #shopping-cart-entity }

To work Cloudstate requires that the descriptors of the protobuf's files are explicitly registered.
We have two ways to do this:

* Via Spring Boot by creating a Spring Boot Configuration class and registering these types accordingly. 
* Programmatically way we’ll explain later in @ref:[Conventions and Restrictions](conventions.md).

Here is an example of a suitable configuration class:

@@snip [ShoppingCartEntity.java]($base$/docs/src/tests/paradox/ShoppingCartEntity.java) { #shopping-cart-configuration }

Then write your simple main class in the Spring boot style.

@@snip [ShoppingCartEntity.java]($base$/docs/src/tests/paradox/ShoppingCartEntity.java) { #shopping-cart-main }

@@@ note { title=Important } In Cloudstate Spring Boot support it is not necessary to register your entities explicitly, instead you just use the annotation **@EnableCloudstate** to tell Spring what to do @@@

Then run the application in the same way as you would any other springboot application with ```mvn spring-boot:run``` command.
