package io.github.NK8916;

import io.github.NK8916.hashImplementations.Murmur3HashFunction;

public class Main {
    public static void main(String[] args) {
        Node[] nodes= NodeGenerator.generateNode(50000).toArray(new Node[0]);
        long startBuilding = System.nanoTime();
        ConsistentHashing router=new ConsistentHashingBuilder().withVersion(1).withHash(new Murmur3HashFunction()).withNodes(nodes).withVNodes(3000).build();
        long endBuilding = System.nanoTime();
        double elapsedSeconds = (endBuilding - startBuilding) / 1_000_000_000.0;
        System.out.println("Elapsed time in building: " + elapsedSeconds + " seconds");
        long startFetching = System.nanoTime();
        Node node=router.getNodeForKey("user:123");
        long endFetching = System.nanoTime();
        double elapsedSecondsInFetching = (endFetching - startFetching) / 1_000_000_000.0;
        System.out.println("Elapsed time in fetching a node: " + elapsedSecondsInFetching + " seconds");
        System.out.println("node: "+node.getId());
    }
}
