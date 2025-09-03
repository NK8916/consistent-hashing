package io.github.NK8916;

import java.util.ArrayList;

public final class RingSnapshot {
    private final long version;
    private final long[] points;
    private final Node[] nodes;

    public RingSnapshot(long version,long[] points,Node[] nodes){
        this.version=version;
        this.points=points;
        this.nodes=nodes;
    }

    public long getVersion(){
        return version;
    }

    public long getSize(){
        return points.length;
    }

    public Node route(long keyHash){
        int i=lowerBound(points,keyHash);
        if(i==getSize()){
            i=0;
        }
        return nodes[i];
    }

    public Node[] routeN(long keyHash,int replicas){
        int i=lowerBound(points,keyHash);
        ArrayList<Node> replicaNodes=new ArrayList<Node>(Math.min(replicas, nodes.length));
        String lastId=null;
        for(int seen=0,p=0;seen<replicas && p<nodes.length;p++){
            Node n=nodes[(i+p)%nodes.length];
            if(replicaNodes.isEmpty() || !n.getId().equals(lastId)){
                replicaNodes.add(n);
                lastId=n.getId();
                seen++;
            }
        }
        return replicaNodes.toArray(new Node[0]);
    }

    private int lowerBound(long[] points,long keyHash){
        int low=0,high=points.length;

        while(low<high){
            int mid=(low+high)>>1;
            if(points[mid]<keyHash){
                low=mid+1;
            }else{
                high=mid;
            }
        }
        return low;
    }
}
