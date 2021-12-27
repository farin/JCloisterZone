package com.jcloisterzone.random;

public class RandomGenerator {

    // https://en.wikipedia.org/wiki/Linear_congruential_generator
    private static final long m = 281474976710655L; // 2 ^ 48 - 1
    private static final long a = 25214903917L;
    private static final long c = 11L;

    /* usually only one integer is needed
    except situations like drawing bazaar tiles
    then use linear congruential generator approach and derive multiple random number from [0,1] seed
    */

    private double random;

    public RandomGenerator(double random) {
        this.random = random;
    }

    public void setRandom(double random) {
        this.random = random;
    }

    public int getNextInt(int bound) {
        int next = (int) (random * bound);
        long x = Double.doubleToLongBits(random) & m;
        // System.err.println(random + " / " + next + " > " + x + " " + ((a * x + c) & m) + " > " + (((a * x + c) & m) / (double) (m + 1)));
        x = (a * x + c) & m;
        random = x / (double) (m + 1);
        return next;
    }
}
