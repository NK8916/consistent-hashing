package io.github.NK8916;

import java.util.*;

public final class RingSnapshot {
    private final long version;
    public final long[] points;
    public final Node[] nodes;
    private final Node[] distinctNodes;

    static long toUnsignedOrder(long x) { return x ^ Long.MIN_VALUE; }

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
        int i=lowerBound(points,toUnsignedOrder(keyHash));
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
        int lo = 0, hi = points.length - 1, ans = -1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            if (Long.compareUnsigned(points[mid], keyHash) >= 0) { ans = mid; hi = mid - 1; }
            else lo = mid + 1;
        }
        return Math.max(ans, 0);
    }
}
