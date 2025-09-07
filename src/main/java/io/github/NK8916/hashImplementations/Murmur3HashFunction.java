package io.github.NK8916.hashImplementations;

import com.google.common.hash.Hashing;

import io.github.NK8916.HashFunction;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class Murmur3HashFunction implements HashFunction{
    public long hash(String key){
        byte[] b = Hashing.murmur3_128(0).hashString(key, StandardCharsets.UTF_8).asBytes(); // 16 bytes
        long lo = ByteBuffer.wrap(b, 0, 8).order(ByteOrder.LITTLE_ENDIAN).getLong();
        long hi = ByteBuffer.wrap(b, 8, 8).order(ByteOrder.LITTLE_ENDIAN).getLong();
        // mix both lanes; keep distribution but avoid lane-bias
        return lo ^ Long.rotateLeft(hi, 1);
    }
}
