package io.github.NK8916;

import io.github.NK8916.hashImplementations.Murmur3HashFunction;

public class Main {
    public static void main(String[] args) {
        HashFunction hashFunction=new Murmur3HashFunction();
        ConsistentHashing ring =new ConsistentHashingBuilder().withHashFunction(hashFunction).build();

    }
}
