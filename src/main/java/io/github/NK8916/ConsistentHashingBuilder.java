package io.github.NK8916;
import java.util.*;
import java.util.stream.Collectors;

import it.unimi.dsi.fastutil.longs.LongArrays;

public final class ConsistentHashingBuilder {
    private Node[] nodes;
    private long[] points;
    private int[] order;
    private Node[] owners;
    private HashFunction hashFunction;
    private int vNodes;
    private long version;


    public ConsistentHashingBuilder withVersion(long version){
        this.version=version;
        return this;
    }

    public ConsistentHashingBuilder withHash(HashFunction hashFunction){
        this.hashFunction=hashFunction;
        return this;
    }

    public ConsistentHashingBuilder withNodes(Node[] nodes){
        this.nodes= nodes;
        return this;
    }

    public ConsistentHashingBuilder withVNodes(int vNodes){
        this.vNodes=vNodes;
        return this;
    }

    public ConsistentHashing build(){
        buildVNodeHash();
        sortHash();
        long[] outPoints=new long[this.points.length];
        Node[] outNodes=new Node[this.points.length];
        for(int i=0;i<this.points.length;i++){
            int j=this.order[i];
            outPoints[i]=this.points[j];
            outNodes[i]=this.owners[j];
        }
        RingSnapshot snapshot=new RingSnapshot(this.version,outPoints,outNodes,this.nodes);
        return new ConsistentHashing(this.hashFunction,snapshot);
    }

    private void buildVNodeHash(){
        Map<String, Node> uniq = Arrays.stream(this.nodes)
                .collect(Collectors.toMap(Node::getId, n -> n, (a,b)->a, LinkedHashMap::new));
        Node[] ownersUnique = uniq.values().toArray(new Node[0]);
        final int n=ownersUnique.length;
        final long totalLong=(long) n * (long) this.vNodes;

        if(totalLong>Integer.MAX_VALUE){
            throw new IllegalArgumentException("Too many vnodes: " + totalLong);
        }

        final int total=(int) totalLong;
        this.points=new long[total];
        this.order=new int[total];
        this.owners=new Node[total];
        int k=0;
        for (Node node : ownersUnique) {
            for (int j = 0; j < this.vNodes; j++) {
                String label = node.getId() + "/" + j;
                long h = this.hashFunction.hash(label);
                this.owners[k] = node;
                this.order[k] = k;
                this.points[k] = h;
                k++;
            }
        }
    }

    private void sortHash(){
        long[] unsignedKeys = new long[this.points.length];
        for (int i = 0; i < this.points.length; i++) {
            unsignedKeys[i] = this.points[i] ^ Long.MIN_VALUE;  // monotone map: unsigned→signed
        }
        LongArrays.radixSortIndirect(this.order,unsignedKeys,true);
    }
}
