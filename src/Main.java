import graph.DirectedGraph;
import graph.Edge;
import graph.Graph;
import mst.Kruskal;
import mst.MstResult;
import mst.Prim;
import scc.Kosaraju;
import shortestpath.Dijkstra;
import topo.TopologicalSort;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Graph city = new Graph(4);
        city.addEdge(0, 1, 2);
        city.addEdge(0, 2, 8);
        city.addEdge(1, 2, 3);
        city.addEdge(1, 3, 7);
        city.addEdge(2, 3, 1);

        System.out.println("Sorted roads:");
        for (Edge edge : new Kruskal().sortedEdges(city)) {
            System.out.println(edge);
        }

        MstResult kruskal = new Kruskal().findMST(city);
        MstResult prim = new Prim().findMST(city);
        System.out.println("Kruskal weight: " + kruskal.getTotalWeight());
        System.out.println("Prim weight: " + prim.getTotalWeight());
        System.out.println("Connected: " + kruskal.isConnected());
        System.out.println("MST weights match: "
                + (kruskal.getTotalWeight() == prim.getTotalWeight()));

        DirectedGraph tasks = new DirectedGraph(5);
        tasks.addEdge(0, 1);
        tasks.addEdge(1, 2);
        tasks.addEdge(1, 3);
        tasks.addEdge(2, 4);
        tasks.addEdge(3, 4);
        System.out.println("Task order: " + new TopologicalSort().sort(tasks));

        DirectedGraph cycles = new DirectedGraph(5);
        cycles.addEdge(0, 1);
        cycles.addEdge(1, 2);
        cycles.addEdge(2, 0);
        cycles.addEdge(2, 3);
        cycles.addEdge(3, 4);
        cycles.addEdge(4, 3);
        try {
            new TopologicalSort().sort(cycles);
        } catch (IllegalStateException exception) {
            System.out.println(exception.getMessage());
        }
        List<List<Integer>> components = new Kosaraju().findSCC(cycles);
        System.out.println("SCC: " + components);

        Dijkstra.Result routes = new Dijkstra().shortestPaths(city, 0);
        for (int vertex = 0; vertex < city.getVertexCount(); vertex++) {
            System.out.println("0 -> " + vertex + ": distance="
                    + routes.distanceTo(vertex) + ", path=" + routes.pathTo(vertex));
        }

        Graph separate = new Graph(4);
        separate.addEdge(0, 1, 2);
        separate.addEdge(2, 3, 1);
        MstResult forest = new Prim().findMST(separate);
        System.out.println("Separate graph connected: " + forest.isConnected());
        System.out.println("Forest components: " + forest.getComponentCount());
        Dijkstra.Result partial = new Dijkstra().shortestPaths(separate, 0);
        System.out.println("Vertex 3 reachable: " + partial.isReachable(3));
        System.out.println("Path to unreachable vertex: " + partial.pathTo(3));
    }
}
