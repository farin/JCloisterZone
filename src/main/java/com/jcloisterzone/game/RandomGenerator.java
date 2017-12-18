package com.jcloisterzone.game;

import java.util.Random;

public class RandomGenerator {

    private final Random random;
    private long salt = 0;

    public RandomGenerator(long seed) {
        random = new Random(seed);
    }

    public void setSalt(long salt) {
        this.salt = salt;
    }

    public long getSalt() {
        return salt;
    }

    public int nextInt(int bound) {
        return (random.nextInt(bound) + ((int)(salt % bound))) % bound;
    }
}