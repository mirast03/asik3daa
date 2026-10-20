package mst;

import graph.Edge;
import graph.Graph;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Kruskal {
    public List<Edge> sortedEdges(Graph graph) {
        List<Edge> edges = new ArrayList<>(graph.getEdges());
        edges.sort(Comparator.comparingInt(Edge::getWeight));
        return edges;
    }
}