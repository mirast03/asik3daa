import graph.Edge;
import graph.Graph;
import dsu.DisjointSet;
import mst.Kruskal;
import mst.MstResult;
import mst.Prim;
import graph.DirectedGraph;
public class Main {
    public static void main(String[] args) {

        Graph city = new Graph(4);

        city.addEdge(0, 1, 2);
        city.addEdge(0, 2, 8);
        city.addEdge(1, 2, 3);
        city.addEdge(1, 3, 7);
        city.addEdge(2, 3, 1);


        Kruskal kruskal = new Kruskal();
        System.out.println("Sorted roads:");
        for (Edge road : kruskal.sortedEdges(city)) {
            System.out.println(road.getFrom() + " - " + road.getTo()
                    + ", cost: " + road.getWeight());
        }
        DisjointSet sets = new DisjointSet(4);
        MstResult kruskalResult = kruskal.findMST(city);
        System.out.println("Kruskal weight: " + kruskalResult.getTotalWeight());
        System.out.println("Connected: " + kruskalResult.isConnected());
        System.out.println("Components: " + kruskalResult.getComponentCount());
        for (Edge road : kruskalResult.getEdges()) {
            System.out.println(road);
        }

        sets.union(0, 1);
        sets.union(2, 3);
        MstResult primResult = new Prim().findMST(city);
        System.out.println("Prim weight: " + primResult.getTotalWeight());
        System.out.println("MST weights match: "
                + (primResult.getTotalWeight() == kruskalResult.getTotalWeight()));
        DirectedGraph tasks = new DirectedGraph(5);
        tasks.addEdge(0, 1);
        tasks.addEdge(1, 2);
        tasks.addEdge(1, 3);
        tasks.addEdge(2, 4);
        tasks.addEdge(3, 4);
        System.out.println("Neighbors of 1: " + tasks.getNeighbors(1));


    }
}