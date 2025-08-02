package io.github.NK8916;

import io.github.NK8916.hashImplementations.MD5HashFunction;

public class ConsistentHashingBuilder {
    private HashFunction hashFunction;
    public ConsistentHashingBuilder withHashFunction(HashFunction hashFunction){
        this.hashFunction=hashFunction;
        return this;
    }

    public ConsistentHashing build(){
        if(this.hashFunction==null){
            hashFunction= new MD5HashFunction();
        }
        return new ConsistentHashing(hashFunction);
    }
}
