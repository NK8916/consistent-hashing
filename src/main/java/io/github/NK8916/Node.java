package io.github.NK8916;

import java.util.Map;

public class Node {
    private final String id;
    private final String ipAddress;
    private final int port;
    private final String region;
    private final Map<String,String> metaData;

    public Node(String id, String ipAddress, int port, String region, Map<String, String> metaData) {
        this.id = id;
        this.ipAddress = ipAddress;
        this.port = port;
        this.region = region;
        this.metaData = metaData;
    }

    public String getId() {
        return id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }

    public String getRegion() {
        return region;
    }

    public Map<String, String> getMetaData() {
        return metaData;
    }
}
