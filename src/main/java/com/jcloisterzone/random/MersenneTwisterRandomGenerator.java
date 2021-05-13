package com.jcloisterzone.random;

import org.apache.commons.math3.random.MersenneTwister;

import java.util.Random;

public class MersenneTwisterRandomGenerator implements RandomGenerator {

    private final MersenneTwister random;
    private long salt = 0;

    public MersenneTwisterRandomGenerator(long seed) {
        random = new MersenneTwister(seed);
    }

    public void setSalt(long salt) {
        this.salt = salt;
    }

    public long getSalt() {
        return salt;
    }

    public int nextInt(int bound) {
        return (int)(Math.abs(salt ^ random.nextLong()) % bound);
    }
}
