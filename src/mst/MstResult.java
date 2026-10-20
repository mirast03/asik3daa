package mst;

import graph.Edge;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MstResult {
    private final List<Edge> edges;
    private final long totalWeight;
    private final int componentCount;

    public MstResult(int vertexCount, List<Edge> selected) {
        edges = Collections.unmodifiableList(new ArrayList<>(selected));
        long total = 0;
        for (Edge edge : edges) { total += edge.getWeight(); }
        totalWeight = total;
        componentCount = vertexCount - edges.size();
    }

    public List<Edge> getEdges() { return edges; }
    public long getTotalWeight() { return totalWeight; }
    public int getComponentCount() { return componentCount; }
    public boolean isConnected() { return componentCount == 1; }
}
