package io.github.NK8916;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ConsistentHashing {
    private final TreeMap<BigInteger, VirtualNode> ring = new TreeMap<>();
    private final HashMap<VirtualNode,Node> virtualNodeMap=new HashMap<>();
    private final int MAX_VIRTUAL_NODES=300;
    private final String SEPERATOR="#";
    private final HashFunction hashFunction;

    ConsistentHashing(HashFunction hashFunction){
        this.hashFunction=hashFunction;
    }

//    private void generateVirtualNodeMapping(Node node){
//        for(int i=1;i<=MAX_VIRTUAL_NODES;i++){
//            String virtualNodeId=String.format("%s%s%s",node.getId(),SEPERATOR,i);
//            BigInteger virtualNodeHash=this.hashFunction.hash(virtualNodeId);
//            VirtualNode virtualNode=new VirtualNode(virtualNodeId,virtualNodeHash);
//            ring.put(virtualNodeHash,virtualNode);
//            virtualNodeMap.put(virtualNode,node);
//        }
//    }

//    public void addNode(Node node){
//        generateVirtualNodeMapping(node);
//    }

//    public void remove(Node node){
//        for(Map.Entry<VirtualNode,Node> entry: virtualNodeMap.entrySet()){
//            if(entry.getValue()==node){
//                VirtualNode virtualNode=entry.getKey();
//                BigInteger virtualNodeHash=this.hashFunction.hash(virtualNode.getId());
//                ring.remove(virtualNodeHash);
//                virtualNodeMap.remove(virtualNode);
//            }
//        }
//    }

//    public Node getNode(String key){
//        BigInteger keyHash=this.hashFunction.hash(key);
//        Map.Entry<BigInteger,VirtualNode> entry=ring.ceilingEntry(keyHash);
//        if(entry==null){
//            entry=ring.firstEntry();
//        }
//        VirtualNode virtualNode= entry.getValue();
//        return virtualNodeMap.get(virtualNode);
//    }
}
