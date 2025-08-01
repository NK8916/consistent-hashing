package io.github.NK8916.hashImplementations;

import io.github.NK8916.HashFunction;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5HashFunction implements HashFunction {
    public BigInteger hash(String key){
        try{
            MessageDigest md=MessageDigest.getInstance("MD5");
            byte[] bytes=md.digest(key.getBytes(StandardCharsets.UTF_8));
            return new BigInteger(1,bytes);
        }catch (NoSuchAlgorithmException e){
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
}
