package io.github.NK8916;

import java.math.BigInteger;
import java.util.Map;
import java.util.TreeMap;

public class ConsistentHashing {
    private final TreeMap<BigInteger, String> ring = new TreeMap<>();
    private final HashFunction hashFunction;

    ConsistentHashing(HashFunction hashFunction){
        this.hashFunction=hashFunction;
    }

    public void add(String node){
        BigInteger nodeHash=this.hashFunction.hash(node);
        ring.put(nodeHash,node);
    }

    public void remove(String node){
        BigInteger nodeHash=this.hashFunction.hash(node);
        ring.remove(nodeHash);
    }

    public String getKey(String node){
        BigInteger keyHash=this.hashFunction.hash(node);
        Map.Entry<BigInteger,String> entry=ring.ceilingEntry(keyHash);
        if(entry==null){
            entry=ring.firstEntry();
        }
        return entry.getValue();
    }
}
