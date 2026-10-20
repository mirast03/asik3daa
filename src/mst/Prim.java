package mst;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Prim {
    public MstResult findMST(Graph graph) {
        return findMST(graph, new Metrics());
    }

    public MstResult findMST(Graph graph, Metrics metrics) {
        boolean[] visited = new boolean[graph.getVertexCount()];
        List<Edge> selected = new ArrayList<>();
        PriorityQueue<Edge> queue = new PriorityQueue<>((first, second) -> {
            metrics.comparisons++;
            return Integer.compare(first.getWeight(), second.getWeight());
        });

        for (int start = 0; start < graph.getVertexCount(); start++) {
            if (visited[start]) { continue; }
            visit(start, graph, visited, queue, metrics);

            while (!queue.isEmpty()) {
                Edge road = queue.remove();
                metrics.queuePolls++;
                metrics.edgeExaminations++;
                if (visited[road.getTo()]) { continue; }

                selected.add(road);
                visit(road.getTo(), graph, visited, queue, metrics);
            }
        }
        return new MstResult(graph.getVertexCount(), selected);
    }

    private void visit(int vertex, Graph graph, boolean[] visited,
                       PriorityQueue<Edge> queue, Metrics metrics) {
        visited[vertex] = true;
        for (Edge road : graph.getNeighbors(vertex)) {
            metrics.edgeExaminations++;
            if (!visited[road.getTo()]) {
                queue.add(road);
                metrics.queueOffers++;
            }
        }
    }
}
