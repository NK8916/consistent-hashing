# Consistent Hashing Library

A high-performance, thread-safe Java implementation of consistent hashing with pluggable hash functions and virtual nodes. Designed for distributed systems to evenly distribute keys across nodes while minimizing remapping when nodes are added or removed.

## Features

- **🔧 Pluggable Hash Functions**: Support for MD5, Murmur3, SHA1, or implement your own
- **🎯 Virtual Nodes**: Configurable virtual nodes for improved key distribution  
- **⚡ High Performance**: Optimized for millions of lookups per second
- **🔒 Thread-Safe**: Immutable ring structure with atomic updates
- **📊 Load Balancing**: Even distribution with minimal variance
- **🔄 Minimal Remapping**: Only ~1/N keys remapped when nodes change
- **🧪 Well Tested**: Comprehensive tests for correctness and performance

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Installation

Clone the repository:
```bash
git clone https://github.com/NK8916/consistent-hashing.git
cd consistent-hashing
```

Build the project:
```bash
mvn clean compile
```

### Basic Usage

```java
import io.github.NK8916.*;
import io.github.NK8916.hashImplementations.Murmur3HashFunction;

// Create nodes
Node[] nodes = {
    new Node("node1", "10.0.0.1", 8080, "us-east-1", Map.of()),
    new Node("node2", "10.0.0.2", 8080, "us-east-1", Map.of()),
    new Node("node3", "10.0.0.3", 8080, "us-west-1", Map.of())
};

// Build consistent hash ring
ConsistentHashing router = new ConsistentHashingBuilder()
    .withVersion(1)
    .withHash(new Murmur3HashFunction())
    .withNodes(nodes)
    .withVNodes(150)  // Virtual nodes per physical node
    .build();

// Route keys to nodes
Node node = router.getNodeForKey("user:12345");
System.out.println("Key routed to: " + node.getId());

// Get replicas for fault tolerance
Node[] replicas = router.getReplicasForKey("user:12345", 3);
```

## Advanced Usage

### Custom Hash Function

```java
public class CustomHashFunction implements HashFunction {
    @Override
    public long hash(String key) {
        // Your custom hash implementation
        return yourHashLogic(key);
    }
}

ConsistentHashing router = new ConsistentHashingBuilder()
    .withHash(new CustomHashFunction())
    .withNodes(nodes)
    .build();
```

### Performance Optimization

For high-throughput scenarios:

```java
// Use more virtual nodes for better distribution (trade-off: more memory)
.withVNodes(500)

// Use Murmur3 for best performance/distribution balance  
.withHash(new Murmur3HashFunction())
```

## Performance

The library is optimized for high-performance scenarios:

- **Lookup Speed**: ~50ns per lookup (millions of ops/sec)
- **Memory Efficient**: O(nodes × vNodes) memory usage
- **Build Time**: Linear with total virtual nodes
- **Distribution Quality**: Coefficient of variation < 1%

Run the benchmark:
```bash
mvn test -Dtest=ConsistentHashingBenchmarkTest#benchmark_getNodeForKey_millions
```

## API Reference

### ConsistentHashing

Main class for routing keys to nodes.

#### Methods

- `getNodeForKey(String key)`: Returns the node responsible for the given key
- `getReplicasForKey(String key, int count)`: Returns multiple nodes for replication
- `ringSize()`: Returns the total number of points on the ring
- `nodeCount()`: Returns the number of physical nodes

### ConsistentHashingBuilder

Fluent builder for configuring the hash ring.

#### Methods

- `withVersion(long version)`: Set ring version for debugging
- `withHash(HashFunction hashFunction)`: Set the hash function
- `withNodes(Node[] nodes)`: Set the physical nodes
- `withVNodes(int vNodes)`: Set virtual nodes per physical node

### Node

Represents a physical node in the system.

```java
new Node(String id, String ipAddress, int port, String region, Map<String,String> metadata)
```

## Testing

Run all tests:
```bash
mvn test
```

Run specific test categories:
```bash
# Core functionality tests
mvn test -Dtest=ConsistentHashingCoreTests

# Performance benchmarks
mvn test -Dtest=ConsistentHashingBenchmarkTest
```

## Hash Functions

### Built-in Options

1. **Murmur3HashFunction** (Recommended)
   - Best performance/distribution trade-off
   - Non-cryptographic, very fast
   
2. **MD5HashFunction**
   - Cryptographically secure
   - Slower but excellent distribution
   
3. **SHA1HashFunction**
   - Cryptographically secure
   - Good distribution, moderate speed

### Choosing a Hash Function

- **High Performance**: Use `Murmur3HashFunction`
- **Security Required**: Use `MD5HashFunction` or `SHA1HashFunction`
- **Custom Needs**: Implement `HashFunction` interface

## Architecture

The library uses an immutable ring snapshot approach:

1. **Ring Construction**: Virtual nodes are distributed on a hash ring
2. **Atomic Updates**: Ring snapshots enable thread-safe operations
3. **Binary Search**: O(log n) lookup using sorted ring points
4. **Minimal Remapping**: Only affected keys remap during topology changes

## Use Cases

- **Distributed Caching**: Route cache keys to cache servers
- **Database Sharding**: Distribute data across database shards  
- **Load Balancing**: Distribute requests across service instances
- **CDN Routing**: Route content requests to edge servers
- **Microservices**: Partition work across service instances

## Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Make your changes and add tests
4. Ensure tests pass: `mvn test`
5. Commit your changes: `git commit -m 'Add amazing feature'`
6. Push to the branch: `git push origin feature/amazing-feature`
7. Open a Pull Request

### Development Guidelines

- Follow existing code style
- Add tests for new functionality
- Update documentation as needed
- Ensure backward compatibility

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Author

**Nitin Kumar** - [NK8916](https://github.com/NK8916)

## Acknowledgments

- Inspired by the consistent hashing algorithm used in Amazon's DynamoDB
- Uses Google Guava for Murmur3 hash implementation
- FastUtil library for optimized collections

