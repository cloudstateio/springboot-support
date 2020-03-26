package io.cloudstate.springboot.starter.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "io.cloudstate")
public final class CloudstateProperties {

    private String userFunctionInterface;
    private int userFunctionPort;

    public String getUserFunctionInterface() {
        return userFunctionInterface;
    }

    public void setUserFunctionInterface(String userFunctionInterface) {
        this.userFunctionInterface = userFunctionInterface;
    }

    public int getUserFunctionPort() {
        return userFunctionPort;
    }

    public void setUserFunctionPort(int userFunctionPort) {
        this.userFunctionPort = userFunctionPort;
    }
}
