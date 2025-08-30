package io.github.NK8916.hashImplementations;

import com.google.common.hash.Hashing;

import io.github.NK8916.HashFunction;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class Murmur3HashFunction implements HashFunction{
    public long hash(String key){
        int hash=Hashing.murmur3_32_fixed().hashString(key, StandardCharsets.UTF_8).asInt();
        return hash & 0xffffffffL;
    }
}
