package com.jcloisterzone.random;

import java.util.Random;

public class JavaRandomGenerator implements RandomGenerator {

    private final Random random;
    private long salt = 0;

    public JavaRandomGenerator(long seed) {
        random = new Random(seed);
    }

    public void setSalt(long salt) {
        this.salt = salt;
    }

    public long getSalt() {
        return salt;
    }

    public int nextInt(int bound) {
        return (random.nextInt(bound) + ((int)(Math.abs(salt) % bound))) % bound;
    }
}