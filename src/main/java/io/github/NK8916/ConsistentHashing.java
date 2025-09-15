package io.github.NK8916;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public final class ConsistentHashing {
   private final HashFunction hashFunction;
   private final AtomicReference<RingSnapshot> ringRef;

   public ConsistentHashing(HashFunction hashFunction,RingSnapshot snapshot){
       this.hashFunction=hashFunction;
       this.ringRef=new AtomicReference<>(snapshot);
   }

    public RingSnapshot debugSnapshot() {
        return new RingSnapshot(
                ringRef.get().getVersion(),
                Arrays.copyOf(ringRef.get().points, ringRef.get().points.length),
                Arrays.copyOf(ringRef.get().nodes,  ringRef.get().nodes.length),
                Arrays.copyOf(ringRef.get().distinctNodes, ringRef.get().distinctNodes.length)
        );
    }

   public Node getNodeForKey(String key){
       long h=hashFunction.hash(key);
       return ringRef.get().route(h);
   }

    public Node[] getReplicasForKey(String key,int count){
       long h=hashFunction.hash(key);
       return ringRef.get().routeN(h,count);
    }

    public int ringSize(){
       return ringRef.get().ringSize();
    }

    public int nodeCount(){
        return this.ringRef.get().getNodeCount();
    }

}
