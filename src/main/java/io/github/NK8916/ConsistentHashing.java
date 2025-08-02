package io.github.NK8916;

import java.math.BigInteger;
import java.util.TreeMap;

public class ConsistentHashing {
    private final TreeMap<BigInteger, String> ring = new TreeMap<>();
    private final HashFunction hashFunction;

    ConsistentHashing(HashFunction hashFunction){
        this.hashFunction=hashFunction;

    }
}
