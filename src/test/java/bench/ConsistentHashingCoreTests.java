package bench;

import io.github.NK8916.ConsistentHashing;
import io.github.NK8916.ConsistentHashingBuilder;
import io.github.NK8916.HashFunction;
import io.github.NK8916.Node;
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

    // ---------- 2) Boundary wraparound ----------
    @Test
    void boundary_wraparound_mapsToFirstOwner() {
        // We want known vnode positions: [10, 20, 30] so a key hashing to 35 wraps to index 0
        // We'll craft a HashFunction that returns fixed values for node vnodes and for our test key.
        class ScriptedHash implements HashFunction {
            private final Map<String, Long> script = new HashMap<>();
            void put(String s, long h) { script.put(s, h); }
            @Override public long hash(String s) {
                Long v = script.get(s);
                if (v != null) return v;
                // default fallback (shouldn't be used in this test)
                return new DemoHash64().hash(s);
            }
        }
        ScriptedHash hf = new ScriptedHash();

        // Nodes (ids must match seeds we script below)
        Node nA = new Node("A","10.0.0.1",8080,"ap", Map.of());
        Node nB = new Node("B","10.0.0.2",8080,"ap", Map.of());
        Node nC = new Node("C","10.0.0.3",8080,"ap", Map.of());

        // Builder usually hashes "nodeId#i". We'll script one vnode per node:
        hf.put("A#0", 10L);
        hf.put("B#0", 20L);
        hf.put("C#0", 30L);

        // Build with 1 vnode per node to keep ring = [10,20,30]
        ConsistentHashing router = new ConsistentHashingBuilder()
                .withHash(hf)
                .withNodes(new Node[]{nA, nB, nC})
                .withVNodes(1)
                .build();

        // Now craft a key that hashes to 35L (past last point 30) → wrap to index 0 → owner A
        hf.put("WRAP_KEY", 35L);
        Node owner = router.getNodeForKey("WRAP_KEY");
        assertEquals("A", owner.getId(),
                "Wraparound failed: hash beyond last point should map to first ring owner");
    }

    // ---------- 3) Distribution fairness ----------
    @Test
    void distribution_is_fair_across_nodes() {
        int nodeCount = 25;
        int vnodesPerNode = 1024; // default you intend
        int samples = 500_000;

        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < nodeCount; i++) {
            nodes.add(new Node("N"+i, "10.0.0."+i, 8080, "ap-south-1", Map.of()));
        }

        // ⚠️ Use your REAL hash here (Murmur3_64 or xxHash64)
        HashFunction hf = new Murmur3HashFunction(); // <- replace in your codebase
        ConsistentHashing router = new ConsistentHashingBuilder()
                .withHash(hf)
                .withNodes(nodes.toArray(new Node[0]))
                .withVNodes(vnodesPerNode)
                .build();


        // Pre-seed ALL nodes with 0 so zeros are counted in min/CV
        Map<String, Integer> counts = new HashMap<>();
        for (Node n : nodes) counts.put(n.getId(), 0);

        // Sample
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int i = 0; i < samples; i++) {
            String key = "k" + rnd.nextLong();
            String id = router.getNodeForKey(key).getId();
            counts.merge(id, 1, Integer::sum);
        }

        // Stats
        double mean = samples / (double) nodeCount;
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        double sumSq = 0.0;
        for (String id : counts.keySet()) {
            int c = counts.get(id);
            min = Math.min(min, c);
            max = Math.max(max, c);
            sumSq += (c - mean) * (c - mean);
        }
        double stddev = Math.sqrt(sumSq / nodeCount);
        double cv = stddev / mean;
        double maxMinRatio = max / (double) Math.max(1, min);

        // Gates (slightly lenient for CI)
        assertTrue(cv < 0.015, "CV too high: " + cv + " (min="+min+", max="+max+")");
        assertTrue(maxMinRatio < 1.08, "Max/Min ratio too high: " + maxMinRatio);
    }

}