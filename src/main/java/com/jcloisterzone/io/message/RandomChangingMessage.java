package com.jcloisterzone.io.message;

public interface RandomChangingMessage extends Message {
    Double getRandom();
    void setRandom(Double random);
}
