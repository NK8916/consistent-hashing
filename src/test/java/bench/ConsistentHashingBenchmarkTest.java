// src/test/java/bench/ConsistentHashingBenchmarkTest.java
package bench;

import io.github.NK8916.ConsistentHashing;
import io.github.NK8916.ConsistentHashingBuilder;
import io.github.NK8916.HashFunction;
import io.github.NK8916.Node;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;


/**
 * Benchmark getNodeForKey() over millions of lookups.
 * - Warmup lets the JIT optimize the hot path.
 * - Uses System.nanoTime() and reports in SECONDS (plus ns/op and ops/sec).
 *
 * NOTE: @Disabled by default so normal test runs stay fast.
 * Run it explicitly when you want numbers.
 */
public class ConsistentHashingBenchmarkTest {

    // --- Use your real hash function implementation.
    // If you already have Murmur3_64 or xxHash64, replace this.
    static final class DemoHash64 implements HashFunction {
        @Override public long hash(String key) {
            // Simple (not cryptographic) demo hash; REPLACE with real one in your codebase.
            long x = key.hashCode() * 2862933555777941757L;
            // final mix for a bit more diffusion
            x ^= (x >>> 33); x *= 0xff51afd7ed558ccdL;
            x ^= (x >>> 33); x *= 0xc4ceb9fe1a85ec53L;
            x ^= (x >>> 33);
            return x;
        }
    }

    @EnabledIfSystemProperty(named = "bench", matches = "true")
    @Test
    void benchmark_getNodeForKey_millions() {
        // --- 1) Build a realistic cluster with your Node shape ---
        Node[] nodes = new Node[]{
                new Node("A", "10.0.0.1", 8080, "ap-south-1", Map.of("rack", "r1")),
                new Node("B", "10.0.0.2", 8080, "ap-south-1", Map.of("rack", "r1")),
                new Node("C", "10.0.0.3", 8080, "ap-south-1", Map.of("rack", "r2")),
                new Node("D", "10.0.0.4", 8080, "ap-south-1", Map.of("rack", "r2")),
                new Node("E", "10.0.0.5", 8080, "ap-south-1", Map.of("rack", "r3"))
        };

        HashFunction hf = new DemoHash64(); // Replace with your Murmur3/xxHash64
        ConsistentHashing router = new ConsistentHashingBuilder()
                .withHash(hf)
                .withNodes(nodes)
                .withVNodes(300)
                .build();

        // --- 2) Pre-generate keys to avoid alloc noise in the hot loop ---
        final int KEY_POOL = 100_000;
        String[] keys = new String[KEY_POOL];
        for (int i = 0; i < KEY_POOL; i++) {
            keys[i] = "k" + ThreadLocalRandom.current().nextLong();
        }

        // --- 3) Warm-up (let JIT optimize) ---
        final int WARM_UP = 500_000;
        int blackhole = 0; // prevent dead-code elimination
        for (int i = 0; i < WARM_UP; i++) {
            blackhole += router.getNodeForKey(keys[i % KEY_POOL]).getId().hashCode();
        }

        // --- 4) Timed run ---
        final int N = 5_000_000;
        long start = System.nanoTime();
        for (int i = 0; i < N; i++) {
            blackhole += router.getNodeForKey(keys[i % KEY_POOL]).getId().hashCode();
        }
        long end = System.nanoTime();

        // --- 5) Report (in seconds) ---
        long elapsedNs = end - start;
        double seconds = elapsedNs / 1_000_000_000.0;
        double nsPerOp = (double) elapsedNs / N;
        double opsPerSec = N / seconds;

        // Print nice summary
        System.out.printf("Lookups: %,d%n", N);
        System.out.printf("Elapsed: %.6f s%n", seconds);
        System.out.printf("Avg: %.2f ns/op (%.3f µs/op)%n", nsPerOp, nsPerOp / 1_000.0);
        System.out.printf("Throughput: %.0f ops/s%n", opsPerSec);

        // Use blackhole (so JIT can’t discard work)
        if (blackhole == 42) {
            System.out.println("ignore: " + blackhole);
        }
    }
}
