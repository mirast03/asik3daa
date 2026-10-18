package graph;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Graph {
    private final int vertexCount;
    private final List<Edge> edges;
    public Graph(int vertexCount){
        this.vertexCount=vertexCount;
        this.edges=new ArrayList<>();
    }
    public void addEdge(int from, int to, int weight){
        Edge edge=new Edge(from, to, weight);
        edges.add(edge);
    }
    public int getVertexCount(){
        return vertexCount;
    }
    public List<Edge> getEdges(){
        return new ArrayList<>(edges);
    }
}
