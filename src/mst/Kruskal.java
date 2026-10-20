package mst;

import dsu.DisjointSet;
import graph.Edge;
import graph.Graph;
import metrics.Metrics;
import java.util.ArrayList;
import java.util.List;

public class Kruskal {
    public List<Edge> sortedEdges(Graph graph) {
        return sortedEdges(graph, new Metrics());
    }

    private List<Edge> sortedEdges(Graph graph, Metrics metrics) {
        List<Edge> edges = new ArrayList<>(graph.getEdges());
        edges.sort((first, second) -> {
            metrics.comparisons++;
            return Integer.compare(first.getWeight(), second.getWeight());
        });
        return edges;
    }

    public MstResult findMST(Graph graph) {
        return findMST(graph, new Metrics());
    }

    public MstResult findMST(Graph graph, Metrics metrics) {
        DisjointSet sets = new DisjointSet(graph.getVertexCount(), metrics);
        List<Edge> selected = new ArrayList<>();

        for (Edge road : sortedEdges(graph, metrics)) {
            metrics.edgeExaminations++;
            if (sets.find(road.getFrom()) != sets.find(road.getTo())) {
                selected.add(road);
                sets.union(road.getFrom(), road.getTo());
            }
        }
        return new MstResult(graph.getVertexCount(), selected);
    }
}
