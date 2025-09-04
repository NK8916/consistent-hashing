package io.github.NK8916;

import java.util.*;

public class NodeGenerator {
    private static final Random random=new Random();
    private static final List<String> REGIONS= Arrays.asList("us-east", "us-west", "eu-central", "ap-south");

    public static Node randomNode(){
        String id= UUID.randomUUID().toString();
        String ipAddress=random.nextInt(256)+"."+
                random.nextInt(256) + "." +
                random.nextInt(256) + "." +
                random.nextInt(256);

        int port=8000+random.nextInt(1000);
        String region=REGIONS.get(random.nextInt(REGIONS.size()));

        Map<String, String> metaData = new HashMap<>();
        metaData.put("os", random.nextBoolean() ? "linux" : "windows");
        metaData.put("version", "v" + (1 + random.nextInt(5)));
        metaData.put("rack", "rack-" + (1 + random.nextInt(10)));

        return new Node(id, ipAddress, port, region, metaData);
    }

    public static List<Node> generateNode(int count){
        List<Node> nodes=new ArrayList<>();
        for(int i=0;i<count;i++){
            nodes.add(randomNode());
        }
        return nodes;
    }

}

