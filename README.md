# Consistent Hashing

A high-performance, thread-safe Java implementation of consistent hashing with pluggable hash functions and virtual nodes. Designed for distributed systems to evenly distribute keys and minimize remapping when nodes are added or removed.

## Features
- Pluggable hash functions: MD5, Murmur3, SHA1 (or your own)
- Virtual nodes for improved key distribution
- Efficient node lookup for any key
- Thread-safe, immutable core
- Benchmark and core tests included

## Getting Started

### Requirements
- Java 17 or higher
- Maven

### Build
```sh
mvn clean package
```

### Run Example
```sh
mvn exec:java -Dexec.mainClass="io.github.NK8916.Main"
```

### Usage Example
```java
Node[] nodes = NodeGenerator.generateNode(50000).toArray(new Node[0]);
ConsistentHashing router = new ConsistentHashingBuilder()
    .withVersion(1)
    .withHash(new Murmur3HashFunction())
    .withNodes(nodes)
    .withVNodes(3000)
    .build();
Node node = router.getNodeForKey("user:123");
System.out.println("Node for key: " + node.getId());
```

## Testing
Run all tests:
```sh
mvn test
```

## Contributing
Contributions are welcome! Please open issues or pull requests for improvements or bug fixes.

## License
MIT License. See [LICENSE](LICENSE) for details.

## Author
Nitin Kumar

