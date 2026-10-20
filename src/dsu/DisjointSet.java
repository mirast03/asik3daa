package dsu;

import metrics.Metrics;

public class DisjointSet {
    private final int[] parent;
    private final int[] size;
    private final Metrics metrics;

    public DisjointSet(int vertexCount) {
        this(vertexCount, new Metrics());
    }

    public DisjointSet(int vertexCount, Metrics metrics) {
        if (vertexCount < 0) {
            throw new IllegalArgumentException("Negative vertex count");
        }
        this.metrics = metrics;
        parent = new int[vertexCount];
        size = new int[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            parent[i] = i;
            size[i] = 1;
        }
    }

    public int find(int vertex) {
        metrics.findCalls++;
        if (parent[vertex] != vertex) {
            parent[vertex] = find(parent[vertex]);
        }
        return parent[vertex];
    }

    public void union(int first, int second) {
        metrics.unionCalls++;
        int firstRoot = find(first);
        int secondRoot = find(second);
        if (firstRoot == secondRoot) { return; }

        if (size[firstRoot] < size[secondRoot]) {
            parent[firstRoot] = secondRoot;
            size[secondRoot] += size[firstRoot];
        } else {
            parent[secondRoot] = firstRoot;
            size[firstRoot] += size[secondRoot];
        }
    }
}
