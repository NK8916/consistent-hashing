package io.github.NK8916.hash;

import io.github.NK8916.ConsistentHashing;
import io.github.NK8916.ConsistentHashingBuilder;
import io.github.NK8916.HashFunction;
import io.github.NK8916.Node;
import io.github.NK8916.hashImplementations.Murmur3HashFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.font.MultipleMaster;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConsistentHashingLoadTest {
    private HashFunction hashFunction_;
    private List<Node> nodes_;
    private ConsistentHashing ring_;


    @BeforeEach
    void setUp(){
        hashFunction_=new Murmur3HashFunction();
        nodes_= NodeGenerator.generateNode(25);
        ring_=new ConsistentHashingBuilder().withHashFunction(hashFunction_).build();
        addNodes(nodes_);
    }

    private void addNodes(List<Node> nodes){
        for(Node node:nodes){
            ring_.addNode(node);
        }
    }

    @Test
    void testOnMillionsOfKeys(){

        long MAX_LIMIT=5_000_000L;
        HashMap<String,Long> counts= new HashMap<String, Long>();

        for(int i=1;i<=MAX_LIMIT;i++){
            String key=String.format("user_key:%d",i);
            Node node=ring_.getNode(key);
            counts.put(node.getId(),counts.getOrDefault(node.getId(),0L)+1);
        }

        for(String key: counts.keySet()){
            long count=counts.get(key);
            long ratio=Math.round((float) (count * 100) /MAX_LIMIT);
            assertTrue(ratio>=3 && ratio<=4);
        }

    }
}
