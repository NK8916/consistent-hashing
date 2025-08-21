package io.github.NK8916;

import io.github.NK8916.hashImplementations.Murmur3HashFunction;

import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        HashFunction hashFunction=new Murmur3HashFunction();
        ConsistentHashing ring =new ConsistentHashingBuilder().withHashFunction(hashFunction).build();
        List<Node> sampleNodes = List.of(
                new Node(
                        "NodeA",
                        "10.0.0.1",
                        8001,
                        "us-east-1",
                        Map.of(
                                "weight", "3",
                                "status", "UP",
                                "role", "cache",
                                "zone", "A"
                        )
                ),
                new Node(
                        "NodeB",
                        "10.0.0.2",
                        8002,
                        "eu-west-1",
                        Map.of(
                                "weight", "2",
                                "status", "UP",
                                "role", "storage",
                                "zone", "B"
                        )
                ),
                new Node(
                        "NodeC",
                        "10.0.0.3",
                        8003,
                        "in-south-1",
                        Map.of(
                                "weight", "4",
                                "status", "UP",
                                "role", "db",
                                "zone", "C"
                        )
                )
        );

        for(Node node:sampleNodes){
            ring.addNode(node);
        }

        Node node1= ring.getNode("user123");
        System.out.println("node1: "+node1.getId());
        Node node2= ring.getNode("user345");
        System.out.println("node: "+node2.getId());
    }
}
