package io.cloudstate.springboot.starter.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "io.cloudstate")
public final class CloudstateProperties {
    public final String USER_FUNCTION_INTERFACE_DEFAULT = "127.0.0.1";
    public final int USER_FUNCTION_PORT = 8080;

    private String userFunctionInterface = USER_FUNCTION_INTERFACE_DEFAULT;
    private int userFunctionPort = USER_FUNCTION_PORT;

    private String userFunctionPackageName;

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

    public String getUserFunctionPackageName() {
        return userFunctionPackageName;
    }

    public void setUserFunctionPackageName(String userFunctionPackageName) {
        this.userFunctionPackageName = userFunctionPackageName;
    }
}
