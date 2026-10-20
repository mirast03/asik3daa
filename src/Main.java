import graph.Edge;
import graph.Graph;
import dsu.DisjointSet;
import mst.Kruskal;

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

        sets.union(0, 1);
        sets.union(2, 3);
    }
}