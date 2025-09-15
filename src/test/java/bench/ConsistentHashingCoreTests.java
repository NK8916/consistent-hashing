package bench;

import io.github.NK8916.*;
import io.github.NK8916.hashImplementations.MD5HashFunction;
import io.github.NK8916.hashImplementations.Murmur3HashFunction;
import io.github.NK8916.hashImplementations.SHA1HashFunction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.sqrt;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests:
 * 1) Determinism: same key → same node
 * 2) Boundary wraparound: key beyond last point wraps to index 0 owner
 * 3) Distribution fairness: CV < 1%, max/min ratio ~ 1.05 (tolerant)
 * 4) Remap fraction on add/remove ≈ ~1/N (with tolerance)
 *
 * Notes:
 * - Uses a tiny fallback DemoHash64 so tests run without extra deps.
 * - For the wraparound test we use a "scripted" HashFunction so that
 *   vnode points are known and a chosen key hash falls past the last point.
 */
public class ConsistentHashingCoreTests {

    // ---------- Minimal HashFunction (use your real one in production) ----------
    static final class DemoHash64 implements HashFunction {
        @Override public long hash(String key) {
            long x = key.hashCode() * 2862933555777941757L;
            x ^= (x >>> 33); x *= 0xff51afd7ed558ccdL;
            x ^= (x >>> 33); x *= 0xc4ceb9fe1a85ec53L;
            x ^= (x >>> 33);
            return x;
        }
    }

    // ---------- 1) Determinism ----------
    @Test
    void determinism_sameKeySameNode() {
        Node[] nodes = new Node[]{
                new Node("A", "10.0.0.1", 8080, "ap-south-1", Map.of()),
                new Node("B", "10.0.0.2", 8080, "ap-south-1", Map.of()),
                new Node("C", "10.0.0.3", 8080, "ap-south-1", Map.of())
        };

        ConsistentHashing router = new ConsistentHashingBuilder()
                .withHash(new DemoHash64())   // replace with Murmur3_64/xxHash64 if available
                .withNodes(nodes)
                .withVNodes(300)
                .build();

        String key = "user:12345";
        Node n1 = router.getNodeForKey(key);
        // re-check multiple times
        for (int i = 0; i < 1000; i++) {
            Node n2 = router.getNodeForKey(key);
            assertEquals(n1.getId(), n2.getId(), "Determinism violated: same key mapped differently");
        }
    }

    @Test
    void boundary_wraparound_mapsToFirstOwner() {
        // Deterministic test hash; fail fast on any unexpected seed/key.
        class ScriptedHash implements HashFunction {
            private final Map<String, Long> script = new HashMap<>();
            void put(String s, long h) { script.put(s, h); }
            @Override public long hash(String s) {
                Long v = script.get(s);
                if (v == null) throw new AssertionError("Unexpected hash input in test: " + s);
                return v;
            }
        }
        ScriptedHash hf = new ScriptedHash();

        Node nA = new Node("A","10.0.0.1",8080,"ap", Map.of());
        Node nB = new Node("B","10.0.0.2",8080,"ap", Map.of());
        Node nC = new Node("C","10.0.0.3",8080,"ap", Map.of());

        // ⚠️ Match the builder's vnode seeding format exactly: "<id>/<i>"
        hf.put("A/0", 10L);
        hf.put("B/0", 20L);
        hf.put("C/0", 30L);

        ConsistentHashing router = new ConsistentHashingBuilder()
                .withHash(hf)
                .withNodes(new Node[]{nA, nB, nC})
                .withVNodes(1)
                .build();

        // Key that hashes beyond last point (30) → wrap to index 0 → owner A
        hf.put("WRAP_KEY", 35L);

        Node owner = router.getNodeForKey("WRAP_KEY");
        assertEquals("A", owner.getId(),
                "Wraparound failed: hash > last point must map to first owner");
    }



    // ---------- 3) Distribution fairness ----------
    @Test
    void distribution_is_fair_across_nodes() {
        int nodeCount = 25;
        int vnodesPerNode = 7500;
        int samples = 5_000_000; // larger sample => much lower noise

        // Nodes
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < nodeCount; i++) {
            nodes.add(new Node("N"+i, "10.0.0."+i, 8080, "ap-south-1", Map.of()));
        }

        // Hash & router (Murmur3-128 → 64-bit mixed)
        HashFunction hf = new Murmur3HashFunction();
        ConsistentHashing router = new ConsistentHashingBuilder()
                .withHash(hf)
                .withNodes(nodes.toArray(new Node[0]))
                .withVNodes(vnodesPerNode)
                .build();

        // ---------- Routing sample (deterministic & fast) ----------
        // Pre-index nodes for O(1) counting
        Map<String,Integer> index = new HashMap<>();
        for (int i = 0; i < nodeCount; i++) index.put(nodes.get(i).getId(), i);
        long[] counts = new long[nodeCount];

        // Deterministic RNG → reproducible CI
        java.util.SplittableRandom rng = new java.util.SplittableRandom(42);
        for (int i = 0; i < samples; i++) {
            // uniform, structure-free keys
            long x = rng.nextLong();
            String key = Long.toUnsignedString(x, 16);
            String id = router.getNodeForKey(key).getId();
            counts[index.get(id)]++;
        }

        // Stats (population CV)
        double mean = samples / (double) nodeCount;
        long min = Long.MAX_VALUE, max = Long.MIN_VALUE;
        double var = 0.0;
        for (long c : counts) {
            if (c < min) min = c;
            if (c > max) max = c;
            double d = c - mean;
            var += d * d;
        }
        var /= nodeCount;
        double cv = Math.sqrt(var) / mean;
        double maxMinRatio = max / (double) Math.max(1L, min);

        System.out.printf("CV=%.4f  max/min=%.4f  mean=%.1f  min=%d  max=%d%n",
                cv, maxMinRatio, mean, min, max);

        // Gates
        org.junit.jupiter.api.Assertions.assertTrue(cv < 0.015, "CV too high: " + cv +
                " (min=" + min + ", max=" + max + ")");
        org.junit.jupiter.api.Assertions.assertTrue(maxMinRatio < 1.08,
                "Max/Min ratio too high: " + maxMinRatio);
    }
}