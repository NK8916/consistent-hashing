package io.github.NK8916.hash;
import io.github.NK8916.HashFunction;
import java.math.BigInteger;
import java.util.Map;

public class FakeHashFunction implements HashFunction {
    private final Map<String, BigInteger> table;
    private final BigInteger defaultHash;

    public FakeHashFunction(Map<String, BigInteger> table, BigInteger defaultHash) {
        this.table = table;
        this.defaultHash = defaultHash;
    }

    @Override
    public BigInteger hash(String key){
        return table.getOrDefault(key,defaultHash);
    }
}
