package io.github.NK8916;
import java.util.ArrayList;
import java.util.List;
import it.unimi.dsi.fastutil.longs.LongArrays;

public final class ConsistentHashingBuilder {
    private List<Node> nodes;
    private List<Long> points;
    private List<Integer> order;
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

    public ConsistentHashingBuilder withNodes(List<Node> nodes){
        this.nodes=nodes;
        return this;
    }

    public ConsistentHashingBuilder withVNodes(Node node,int vNodes){
        this.vNodes=vNodes;
        return this;
    }

    public RingSnapshot build(){
        buildVNodeHash();
        sortHash();
        long[] outPoints=new long[this.points.size()];
        Node[] outNodes=new Node[this.points.size()];

        for(int i=0;i<points.size();i++){
            outPoints[i]=points.get(this.order.get(i));
            outNodes[i]=nodes.get(this.order.get(i));
        }
        return new RingSnapshot(this.version,outPoints,outNodes);
    }

    private void buildVNodeHash(){
        for(Node node:this.nodes){
            for(int i=0;i<this.vNodes;i++) {
                this.nodes.add(node);
                this.order.add(i);
                String key = String.format("%s%s%S", node.getId(), SEPERATOR, i);
                this.points.add(this.hashFunction.hash(key));
            }
        }
    }

    private void sortHash(){
        LongArrays.radixSortIndirect(this.order.stream()
                .mapToInt(Integer::intValue)
                .toArray(),points.stream()
                .mapToLong(Long::longValue)
                .toArray(),true);
    }
}
