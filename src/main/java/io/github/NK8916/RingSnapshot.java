package io.github.NK8916;

import java.util.*;

public final class RingSnapshot {
    private final long version;
    private final long[] points;
    private final Node[] nodes;
    private final Node[] distinctNodes;

    public RingSnapshot(long version,long[] points,Node[] nodes,Node[] distinctNodes){
        this.version=version;
        this.points=points;
        this.nodes=nodes;
        this.distinctNodes=distinctNodes;
    }

    public long getVersion(){
        return version;
    }

    public long getSize(){
        return points.length;
    }

    public int getNodeCount(){
        return this.distinctNodes.length;
    }

    public Node route(long keyHash){
        int i=lowerBound(points,keyHash);
        if(i==getSize()){
            i=0;
        }
        return nodes[i];
    }

    public Node[] routeN(long keyHash,int replicas){
        int idx = lowerBound(points,keyHash);

        List<Node> out = new ArrayList<>(replicas);
        Set<String> seen = new HashSet<>(replicas);
        for (int i = 0; i < nodes.length && out.size() < replicas; i++) {
            Node candidate = nodes[(idx + i) % nodes.length];
            if (seen.add(candidate.getId())) {
                out.add(candidate);
            }
        }
        return out.toArray(new Node[0]);
    }

    public int ringSize(){
        return this.points.length;
    }

    private int lowerBound(long[] points,long keyHash){
        int idx = Arrays.binarySearch(points, keyHash);
        if (idx < 0) {
            idx = -(idx + 1);
            if (idx == points.length) {
                idx = 0;
            }
        }
        return idx;
    }
}
