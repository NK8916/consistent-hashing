package io.github.NK8916.hash;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.NK8916.ConsistentHashing;
import io.github.NK8916.ConsistentHashingBuilder;
import io.github.NK8916.HashFunction;
import io.github.NK8916.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class ConsistentHashRingTest {
    private Map<String, BigInteger> table;
    private FakeHashFunction hasher;

    private Node node1;
    private Node node2;
    private Node node3;

    @BeforeEach
    void setup(){
        table = new HashMap<>();
        table.put("node-1#0", BigInteger.valueOf(100));
        table.put("node-1#1", BigInteger.valueOf(300));
        table.put("node-1#2", BigInteger.valueOf(700));
        table.put("node-1#3", BigInteger.valueOf(1100));

        table.put("node-2#0", BigInteger.valueOf(200));
        table.put("node-2#1", BigInteger.valueOf(600));
        table.put("node-2#2", BigInteger.valueOf(900));
        table.put("node-2#3", BigInteger.valueOf(1300));

        table.put("k0", BigInteger.ZERO);
        table.put("k1", BigInteger.valueOf(100));
        table.put("k2", BigInteger.valueOf(500));
        table.put("k3", BigInteger.valueOf(900));
        table.put("kWrap", new BigInteger("18446744073709551615"));
        table.put("kCollisionA", BigInteger.valueOf(4242));
        table.put("kCollisionB", BigInteger.valueOf(4242));

        hasher = new FakeHashFunction(table, BigInteger.valueOf(777));

        node1 = new Node("node-1", "10.0.0.1", 8080, "ap-south-1", Map.of("tier", "gold"));
        node2 = new Node("node-2", "10.0.0.2", 8080, "ap-south-1", Map.of("tier", "silver"));
        node3 = new Node("node-3", "10.0.0.3", 8080, "ap-south-1", Map.of("tier", "bronze"));
    }

    @Test
    void whenOnlyOneNodePresent(){
        ConsistentHashing ring =new ConsistentHashingBuilder().withHashFunction(hasher).build();
        ring.addNode(node1);

        for(String key: table.keySet()){
            assertEquals(node1, ring.getNode(key));
        }
    }

    @Test
    void keysRedistributionTest(){

        ConsistentHashing ring =new ConsistentHashingBuilder().withHashFunction(hasher).build();
        ring.addNode(node1);

        Map<String,Node> beforeMap=new HashMap<>();

        for(String key:table.keySet()){
            beforeMap.put(key,ring.getNode(key));
        }

        ring.addNode(node2);

        Map<String,Node> afterMap=new HashMap<>();

        for(String key:table.keySet()){
            afterMap.put(key,ring.getNode(key));
        }

        int changeKeys=0;

        for(String k: beforeMap.keySet()){
            if(!Objects.equals(beforeMap.get(k),afterMap.get(k))){
                changeKeys++;
            }
        }

        assertTrue(changeKeys>0 && changeKeys<table.size());
    }

    @Test
    void minimalRemapOnAdditionOrRemoval(){
        ConsistentHashing ring =new ConsistentHashingBuilder().withHashFunction(hasher).build();
        ring.addNode(node1);

        Map<String,Node> beforeMap=new HashMap<>();

        for(String key:table.keySet()){
            beforeMap.put(key,ring.getNode(key));
        }

        ring.addNode(node2);

        Map<String,Node> afterMap=new HashMap<>();

        for(String key:table.keySet()){
            afterMap.put(key,ring.getNode(key));
        }

        int changeKeys=0;

        for(String k: beforeMap.keySet()){
            if(!Objects.equals(beforeMap.get(k),afterMap.get(k))){
                changeKeys++;
            }
        }
        assertTrue(changeKeys>0 && changeKeys<table.size());
    }

    @Test
    void testWrapAroundCondition(){
        HashFunction patternHasher=new PatternHashing();
        ConsistentHashing ring =new ConsistentHashingBuilder().withHashFunction(patternHasher).build();
        ring.addNode(node1);
        ring.addNode(node2);

        assertEquals("node-1", ring.getNode("k_eq_min").getId());
        assertEquals("node-1",ring.getNode("k_lt_min").getId());
        assertEquals("node-2",ring.getNode("k_mid_350").getId());
        assertEquals("node-2",ring.getNode("k_eq_max").getId());
        assertEquals("node-1",ring.getNode("k_gt_max").getId());
    }
}
