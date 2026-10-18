package dsu;

public class DisjointSet {
    private final int[]parent;
    private final int[] size;
    public DisjointSet(int vertexCount) {
        parent = new int[vertexCount];
        size = new int[vertexCount];

        for (int i = 0; i < vertexCount; i++) {
            parent[i] = i;
            size[i] = 1;
        }
    }
    public int find(int vertex) {
        if (parent[vertex] != vertex) {
            parent[vertex] = find(parent[vertex]);
        }

        return parent[vertex];
    }
    public void union(int first, int second) {
        int firstRoot = find(first);
        int secondRoot = find(second);

        if (firstRoot == secondRoot) {
            return;
        }

        if (size[firstRoot] < size[secondRoot]) {
            parent[firstRoot] = secondRoot;
            size[secondRoot] += size[firstRoot];
        } else {
            parent[secondRoot] = firstRoot;
            size[firstRoot] += size[secondRoot];
        }
    }
}
