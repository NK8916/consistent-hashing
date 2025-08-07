package io.github.NK8916;

import java.math.BigInteger;

public class VirtualNode {
    private final String id;
    private final BigInteger hash;

    public VirtualNode(String id, BigInteger hash) {
        this.id = id;
        this.hash = hash;
    }

    public String getId() {
        return id;
    }

    public BigInteger getHash() {
        return hash;
    }
}
