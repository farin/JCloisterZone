package com.jcloisterzone;

import java.math.BigInteger;
import java.security.SecureRandom;

public class KeyUtils {

    public static String createRandomId() {
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
    }

    public static void main(String[] args) {
        System.out.println(createRandomId());
    }

}
