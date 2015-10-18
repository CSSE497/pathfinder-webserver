package io.pathfinder.util;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class Security {
  public static String generateToken(int length) {
    SecureRandom rand = new SecureRandom();
    byte[] bytes = new byte[length];

    // This self seeds using the OS's random number generator
    rand.nextBytes(bytes);

    // It must be ASCII, UTF standards create different length Strings for some reason
    return new String(bytes, StandardCharsets.US_ASCII);
  }
}
