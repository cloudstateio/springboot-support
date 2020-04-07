package io.cloudstate.springboot.starter.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "io.cloudstate")
public final class CloudstateProperties {
    public final int USER_FUNCTION_PORT = 8080;
    public final String USER_FUNCTION_INTERFACE_DEFAULT = "127.0.0.1";

    private boolean autoRegister = true;
    private boolean autoStartup = true;
    private String userFunctionInterface = USER_FUNCTION_INTERFACE_DEFAULT;
    private int userFunctionPort = USER_FUNCTION_PORT;
    private String userFunctionPackageName;

    public boolean isAutoRegister() {
        return autoRegister;
    }

    public boolean isAutoStartup() {
        return autoStartup;
    }

    public void setAutoStartup(boolean autoStartup) {
        this.autoStartup = autoStartup;
    }

    public void setAutoRegister(boolean autoRegister) {
        this.autoRegister = autoRegister;
    }

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
