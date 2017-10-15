package com.jcloisterzone.plugin;

public class ExternalResourceException extends RuntimeException {

    private final String alias;

    public ExternalResourceException(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

}
