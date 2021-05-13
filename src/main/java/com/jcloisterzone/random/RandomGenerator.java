package com.jcloisterzone.random;

public interface RandomGenerator {

    void setSalt(long salt);
    long getSalt();
    int nextInt(int bound);
}
