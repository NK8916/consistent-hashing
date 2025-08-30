package io.github.NK8916.hashImplementations;

import io.github.NK8916.HashFunction;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA1HashFunction implements HashFunction {
    public long hash(String key){
        try{
            MessageDigest md=MessageDigest.getInstance("SHA-1");
            byte[] bytes=md.digest(key.getBytes(StandardCharsets.UTF_8));
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            return buffer.getLong();
        }catch (NoSuchAlgorithmException e){
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
}
