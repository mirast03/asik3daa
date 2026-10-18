import graph.Edge;
import graph.Graph;
import dsu.DisjointSet;

public class Main {
    public static void main(String[] args) {
        Graph city = new Graph(4);

        city.addEdge(0, 1, 2);
        city.addEdge(0, 2, 8);
        city.addEdge(1, 2, 3);
        city.addEdge(1, 3, 7);
        city.addEdge(2, 3, 1);

        for (Edge road : city.getEdges()) {
            System.out.println(
                    road.getFrom() + " - " + road.getTo()
                            + ", cost: " + road.getWeight()
            );
        }

        DisjointSet sets = new DisjointSet(4);

        sets.union(0, 1);
        sets.union(2, 3);

        System.out.println("Connected 0 and 1: "
                + (sets.find(0) == sets.find(1)));

        System.out.println("Connected 0 and 3: "
                + (sets.find(0) == sets.find(3)));

        sets.union(1, 2);

        System.out.println("After union, connected 0 and 3: "
                + (sets.find(0) == sets.find(3)));
        DisjointSet check = new DisjointSet(4);

        check.union(0, 1);
        check.union(3, 0);

        System.out.println("Root of 3: " + check.find(3));
        DisjointSet compressed = new DisjointSet(4);

        compressed.union(0, 1);
        compressed.union(2, 3);
        compressed.union(0, 2);

        System.out.println("First find: " + compressed.find(3));
        System.out.println("Second find: " + compressed.find(3));
    }
}