package io.github.NK8916.hash;
import static org.junit.jupiter.api.Assertions.assertEquals;
import io.github.NK8916.ConsistentHashing;
import io.github.NK8916.ConsistentHashingBuilder;
import io.github.NK8916.Node;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class ConsistentHashRingTest {

    @Test
    void whenOnlyOneNodePresent(){
        Map<String, BigInteger> fakeHashes=new HashMap<>();
        fakeHashes.put("k1",BigInteger.ZERO);
        fakeHashes.put("k2",BigInteger.valueOf(42));
        fakeHashes.put("k3",BigInteger.valueOf(999_999));
        fakeHashes.put("k4", BigInteger.valueOf(Long.MAX_VALUE));
        fakeHashes.put("k5", new BigInteger("12345678901234567890"));

        FakeHashFunction fakeHasher=new FakeHashFunction(fakeHashes,BigInteger.valueOf(777));

        Node onlyNode = new Node(
                "node-1",
                "10.0.0.1",
                8080,
                "ap-south-1",
                Map.of("tier", "gold")
        );
        ConsistentHashing ring =new ConsistentHashingBuilder().withHashFunction(fakeHasher).build();
        ring.addNode(onlyNode);

        for(String key: fakeHashes.keySet()){
            assertEquals(onlyNode, ring.getNode(key));
        }
    }
}
