package topo;

import graph.DirectedGraph;
import metrics.Metrics;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class TopologicalSort {
    public List<Integer> sort(DirectedGraph graph) {
        return sort(graph, new Metrics());
    }

    public List<Integer> sort(DirectedGraph graph, Metrics metrics) {
        int count = graph.getVertexCount();
        int[] inDegree = new int[count];
        for (int from = 0; from < count; from++) {
            for (int to : graph.getNeighbors(from)) {
                metrics.edgeExaminations++;
                inDegree[to]++;
            }
        }

        Queue<Integer> ready = new ArrayDeque<>();
        for (int vertex = 0; vertex < count; vertex++) {
            if (inDegree[vertex] == 0) {
                ready.add(vertex);
                metrics.queueOffers++;
            }
        }

        List<Integer> order = new ArrayList<>();
        while (!ready.isEmpty()) {
            int vertex = ready.remove();
            metrics.queuePolls++;
            order.add(vertex);
            for (int next : graph.getNeighbors(vertex)) {
                metrics.edgeExaminations++;
                if (--inDegree[next] == 0) {
                    ready.add(next);
                    metrics.queueOffers++;
                }
            }
        }
        if (order.size() != count) {
            throw new IllegalStateException("Graph contains a directed cycle");
        }
        return order;
    }
}
