package io.github.NK8916;

import io.github.NK8916.hashImplementations.Murmur3HashFunction;

import java.math.BigInteger;

public class Main {
    public static void main(String[] args) {
        HashFunction hashFunction=new Murmur3HashFunction();
        ConsistentHashing ring =new ConsistentHashingBuilder().withHashFunction(hashFunction).build();
        ring.add("node1");
        ring.add("node2");
        ring.add("node3");
        ring.add("node4");
        String node1= ring.getKey("user123");
        System.out.println("node1: "+node1);
        String node2= ring.getKey("user345");
        System.out.println("node: "+node2);
    }
}
