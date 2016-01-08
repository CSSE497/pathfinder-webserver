package io.pathfinder.util;

import java.security.SecureRandom;

public class Security {
    public static byte[] generateToken(int length) {
        SecureRandom rand = new SecureRandom();
        byte[] bytes = new byte[length];

        // This self seeds using the OS's random number generator
        rand.nextBytes(bytes);

        // It must be ASCII, UTF standards create different length Strings for some reason
        return bytes;
    }
}
