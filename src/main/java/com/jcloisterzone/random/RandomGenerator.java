package com.jcloisterzone.random;

public class RandomGenerator {

    private double random;

    public RandomGenerator(double random) {
        this.random = random;
    }

    public double getRandom() {
        return random;
    }

    public void setRandom(double random) {
        this.random = random;
    }

    public int getInt(int bound) {
        return (int) (random * bound);
    }
}
