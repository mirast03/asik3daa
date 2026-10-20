package bench;

import graph.DirectedGraph;
import graph.Graph;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class GraphGenerator {
    private static long key(int from, int to, int count) {
        return (long) from * count + to;
    }

    public static Graph connected(int vertices, int edges, long seed) {
        if (vertices < 1 || edges < vertices - 1
                || edges > (long) vertices * (vertices - 1) / 2) {
            throw new IllegalArgumentException("Invalid connected graph size");
        }
        Random random = new Random(seed);
        Graph graph = new Graph(vertices);
        Set<Long> used = new HashSet<>();
        for (int to = 1; to < vertices; to++) {
            int from = random.nextInt(to);
            graph.addEdge(from, to, 1 + random.nextInt(100));
            used.add(key(from, to, vertices));
        }
        while (used.size() < edges) {
            int a = random.nextInt(vertices);
            int b = random.nextInt(vertices);
            if (a == b) { continue; }
            int from = Math.min(a, b);
            int to = Math.max(a, b);
            if (used.add(key(from, to, vertices))) {
                graph.addEdge(from, to, 1 + random.nextInt(100));
            }
        }
        return graph;
    }

    public static DirectedGraph directed(int vertices, int edges,
                                         long seed, boolean dag) {
        long maximum = (long) vertices * (vertices - 1) / (dag ? 2 : 1);
        if (vertices < 1 || edges < 0 || edges > maximum
                || (!dag && (vertices < 2 || edges < 2))) {
            throw new IllegalArgumentException("Invalid directed graph size");
        }
        Random random = new Random(seed);
        DirectedGraph graph = new DirectedGraph(vertices);
        Set<Long> used = new HashSet<>();
        if (!dag) {
            graph.addEdge(0, 1);
            graph.addEdge(1, 0);
            used.add(key(0, 1, vertices));
            used.add(key(1, 0, vertices));
        }
        while (used.size() < edges) {
            int from = random.nextInt(vertices);
            int to = random.nextInt(vertices);
            if (from == to) { continue; }
            if (dag && from > to) {
                int temporary = from;
                from = to;
                to = temporary;
            }
            if (used.add(key(from, to, vertices))) { graph.addEdge(from, to); }
        }
        return graph;
    }
}
