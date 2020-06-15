# Configuration

It is possible to configure certain application parameters either via the conventional springboot configuration file 
( **application.properties** or **application.yaml** ) or via the HOCON configuration file ( **application.conf** ).

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
@@@ note { title=Important } 

Cloudstate Springboot Support uses Classpath scanning to assist in the registration step of entity functions, 
you can explicitly specify in which package the system should look for its entities. 
This can speed up the application's bootstrap by more than a second in most cases.
To define your package use the user-function-package-name property as done in the example above. 
This property is only available for spring configuration files (application.yml or application.properties). 

@@@


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