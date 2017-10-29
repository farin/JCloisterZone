package com.jcloisterzone.plugin;

public class PluginLoadException extends Exception {

    public PluginLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public PluginLoadException(String message) {
        super(message);
    }

    public PluginLoadException(Throwable cause) {
        super(cause);
    }

}
