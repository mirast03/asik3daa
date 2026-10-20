package graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DirectedGraph {
    private final List<List<Integer>> adjacency = new ArrayList<>();
    private int edgeCount;

    public DirectedGraph(int vertexCount) {
        if (vertexCount < 0) {
            throw new IllegalArgumentException("Negative vertex count");
        }
        for (int i = 0; i < vertexCount; i++) {
            adjacency.add(new ArrayList<>());
        }
    }

    public int getVertexCount() { return adjacency.size(); }
    public int getEdgeCount() { return edgeCount; }

    public void addEdge(int from, int to) {
        checkVertex(from);
        checkVertex(to);
        adjacency.get(from).add(to);
        edgeCount++;
    }

    public List<Integer> getNeighbors(int vertex) {
        checkVertex(vertex);
        return Collections.unmodifiableList(adjacency.get(vertex));
    }

    public DirectedGraph reversed() {
        DirectedGraph result = new DirectedGraph(getVertexCount());
        for (int from = 0; from < getVertexCount(); from++) {
            for (int to : getNeighbors(from)) { result.addEdge(to, from); }
        }
        return result;
    }

    private void checkVertex(int vertex) {
        if (vertex < 0 || vertex >= getVertexCount()) {
            throw new IllegalArgumentException("Invalid vertex: " + vertex);
        }
    }
}
