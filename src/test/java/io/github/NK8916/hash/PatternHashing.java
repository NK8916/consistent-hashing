package io.github.NK8916.hash;

import io.github.NK8916.HashFunction;

import java.math.BigInteger;

class PatternHashing implements HashFunction {
    @Override
    public BigInteger hash(String key) {
        if (key.startsWith("node-1#")) {
            int i = Integer.parseInt(key.substring("node-1#".length()));
            return BigInteger.valueOf(100L + 200L * i);
        }

        if (key.startsWith("node-2#")) {
            int i = Integer.parseInt(key.substring("node-2#".length()));
            return BigInteger.valueOf(200L + 200L * i);
        }

        return switch (key) {
            case "k_eq_min" -> BigInteger.valueOf(100);
            case "k_lt_min" -> BigInteger.valueOf(90);
            case "k_mid_350" -> BigInteger.valueOf(350);
            case "k_eq_max" -> BigInteger.valueOf(200 + 200L * 299);
            case "k_gt_max" -> BigInteger.valueOf(200 + 200L * 299 + 1);
            default -> BigInteger.valueOf(777);
        };
    }
}
