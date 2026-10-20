package graph;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Graph {
    private final List<Edge> edges = new ArrayList<>();
    private final List<List<Edge>> adjacency = new ArrayList<>();
    public Graph(int vertexCount){
        if (vertexCount < 0) {
            throw new IllegalArgumentException("Negative vertex count");
        }
        for (int i = 0; i < vertexCount; i++) {
            adjacency.add(new ArrayList<>());
        }
    }
    public void addEdge(int from, int to, int weight){
        checkVertex(from);
        checkVertex(to);
        Edge edge=new Edge(from, to, weight);
        edges.add(edge);
        adjacency.get(from).add(edge);
        adjacency.get(to).add(new Edge(to, from, weight));
    }
    public int getVertexCount(){
        return adjacency.size();
    }
    public List<Edge> getEdges(){
        return Collections.unmodifiableList(edges);
    }
    public List<Edge> getNeighbors(int vertex){
        checkVertex(vertex);
        return Collections.unmodifiableList(adjacency.get(vertex));
    }
    private void checkVertex(int vertex){
        if(vertex<0||vertex>=getVertexCount()){
            throw new IllegalArgumentException("Invalid vertex: "+vertex);
        }
    }
}
