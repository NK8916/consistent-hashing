package io.github.NK8916;
import java.util.ArrayList;
import java.util.List;
import it.unimi.dsi.fastutil.longs.LongArrays;

public final class ConsistentHashingBuilder {
    private Node[] nodes;
    private long[] points;
    private int[] order;
    private Node[] owners;
    private HashFunction hashFunction;
    private int vNodes;
    private final String SEPERATOR="#";
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
            outPoints[i]=this.points[this.order[i]];
            outNodes[i]=this.owners[this.order[i]];
        }
        RingSnapshot snapshot=new RingSnapshot(this.version,outPoints,outNodes);
        return new ConsistentHashing(this.hashFunction,snapshot);
    }

    private void buildVNodeHash(){
        final int n=this.nodes.length;
        final long totalLong=(long) n * (long) this.vNodes;

        if(totalLong>Integer.MAX_VALUE){
            throw new IllegalArgumentException("Too many vnodes: " + totalLong);
        }

        final int total=(int) totalLong;

        this.points=new long[total];
        this.order=new int[total];
        this.owners=new Node[total];
        int k=0;
        for(int i=0;i<n;i++){
            Node node=this.nodes[i];
            String key = String.format("%s%s%S", node.getId(), SEPERATOR, i);
            for(int j=0;i<this.vNodes;i++) {
                this.owners[k]=node;
                this.order[k]=j;
                this.points[k]=this.hashFunction.hash(key);
            }
        }
    }

    private void sortHash(){
        LongArrays.radixSortIndirect(this.order,this.points,true);
    }
}
