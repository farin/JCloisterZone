package com.jcloisterzone.io.message;

public interface SaltMessage extends Message {
    String getSalt();
    void setSalt(String seed);
}
