package tests;

import bench.GraphGenerator;
import dsu.DisjointSet;
import graph.DirectedGraph;
import graph.Edge;
import graph.Graph;
import metrics.Metrics;
import mst.Kruskal;
import mst.MstResult;
import mst.Prim;
import scc.Kosaraju;
import shortestpath.Dijkstra;
import topo.TopologicalSort;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AlgorithmTests {
    private static int checks;

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) { throw new AssertionError(message); }
    }

    public static void main(String[] args) {
        testDsu();
        testMstAndRoutes();
        testDirected();
        testRandomGraphs();
        testGenerator();
        System.out.println("PASS: " + checks + " checks");
    }

    private static void testDsu() {
        Metrics metrics = new Metrics();
        DisjointSet dsu = new DisjointSet(4, metrics);
        dsu.union(0, 1);
        dsu.union(2, 3);
        dsu.union(0, 2);
        long before = metrics.findCalls;
        int first = dsu.find(3);
        long firstCalls = metrics.findCalls - before;
        before = metrics.findCalls;
        int second = dsu.find(3);
        long secondCalls = metrics.findCalls - before;
        check(first == second && first == dsu.find(0), "DSU connectivity");
        check(secondCalls < firstCalls, "Path compression shortens find");
        dsu.union(3, 1);
        check(dsu.find(3) == dsu.find(1), "Repeated union");
        DisjointSet sizes = new DisjointSet(4);
        sizes.union(0, 1);
        sizes.union(3, 0);
        check(sizes.find(3) == 0, "Union by size keeps larger root");
    }

    private static void assertForest(Graph graph, long weight, int components) {
        for (MstResult result : Arrays.asList(
                new Kruskal().findMST(graph), new Prim().findMST(graph))) {
            check(result.getTotalWeight() == weight, "Forest weight");
            check(result.getComponentCount() == components, "Component count");
            check(result.getEdges().size() == graph.getVertexCount() - components,
                    "Forest edge count");
            check(result.isConnected() == (components == 1), "Connected flag");
            DisjointSet dsu = new DisjointSet(graph.getVertexCount());
            for (Edge edge : result.getEdges()) {
                check(dsu.find(edge.getFrom()) != dsu.find(edge.getTo()), "Forest cycle");
                dsu.union(edge.getFrom(), edge.getTo());
            }
        }
    }

    private static void testMstAndRoutes() {
        Graph city = new Graph(4);
        city.addEdge(0, 1, 2); city.addEdge(0, 2, 8);
        city.addEdge(1, 2, 3); city.addEdge(1, 3, 7); city.addEdge(2, 3, 1);
        assertForest(city, 6, 1);
        Dijkstra.Result routes = new Dijkstra().shortestPaths(city, 0);
        long[] expected = {0, 2, 5, 6};
        for (int i = 0; i < 4; i++) {
            check(routes.distanceTo(i) == expected[i], "City distance");
        }
        check(routes.pathTo(3).equals(Arrays.asList(0, 1, 2, 3)), "City route");
        check(routes.pathTo(0).equals(Arrays.asList(0)), "Source route");

        Graph separate = new Graph(5);
        separate.addEdge(0, 1, 2); separate.addEdge(2, 3, 1);
        assertForest(separate, 3, 3);
        Dijkstra.Result partial = new Dijkstra().shortestPaths(separate, 0);
        check(!partial.isReachable(3) && partial.pathTo(3).isEmpty(), "Unreachable path");
        check(partial.distanceTo(4) == Long.MAX_VALUE, "Unreachable distance");
        assertForest(new Graph(1), 0, 1);
        assertForest(new Graph(0), 0, 0);
        check(new Dijkstra().shortestPaths(new Graph(1), 0).distanceTo(0) == 0,
                "Singleton distance");

        Graph equal = new Graph(4);
        equal.addEdge(0, 1, 1); equal.addEdge(1, 2, 1);
        equal.addEdge(2, 3, 1); equal.addEdge(3, 0, 1);
        equal.addEdge(0, 2, 1); equal.addEdge(0, 1, 1);
        equal.addEdge(0, 0, 0);
        assertForest(equal, 3, 1);

        Graph zero = new Graph(3);
        zero.addEdge(0, 1, 0); zero.addEdge(1, 2, 0); zero.addEdge(0, 2, 5);
        check(new Dijkstra().shortestPaths(zero, 0).distanceTo(2) == 0, "Zero weights");
        Graph large = new Graph(3);
        large.addEdge(0, 1, Integer.MAX_VALUE);
        large.addEdge(1, 2, Integer.MAX_VALUE);
        check(new Dijkstra().shortestPaths(large, 0).distanceTo(2) == 4294967294L,
                "Long distances");

        Graph negative = new Graph(2);
        negative.addEdge(0, 1, -2);
        assertForest(negative, -2, 1);
        boolean rejected = false;
        try { new Dijkstra().shortestPaths(negative, 0); }
        catch (IllegalArgumentException expectedError) { rejected = true; }
        check(rejected, "Reject negative Dijkstra weights");
    }

    private static void validOrder(DirectedGraph graph, List<Integer> order) {
        check(order.size() == graph.getVertexCount(), "Topo length");
        int[] position = new int[graph.getVertexCount()];
        Arrays.fill(position, -1);
        for (int i = 0; i < order.size(); i++) {
            int vertex = order.get(i);
            check(vertex >= 0 && vertex < position.length && position[vertex] == -1,
                    "Topo unique vertices");
            position[vertex] = i;
        }
        for (int from = 0; from < position.length; from++) {
            for (int to : graph.getNeighbors(from)) {
                check(position[from] < position[to], "Topo edge direction");
            }
        }
    }

    private static void testDirected() {
        validOrder(new DirectedGraph(0), new TopologicalSort().sort(new DirectedGraph(0)));
        validOrder(new DirectedGraph(1), new TopologicalSort().sort(new DirectedGraph(1)));
        DirectedGraph graph = new DirectedGraph(6);
        graph.addEdge(0, 1); graph.addEdge(1, 2); graph.addEdge(2, 0);
        graph.addEdge(2, 3); graph.addEdge(3, 4); graph.addEdge(4, 3);
        verifyScc(graph);
        check(new Kosaraju().findSCC(graph).size() == 3, "SCC including isolated vertex");
        boolean rejected = false;
        try { new TopologicalSort().sort(graph); }
        catch (IllegalStateException expected) { rejected = true; }
        check(rejected, "Directed cycle detection");
        DirectedGraph selfLoop = new DirectedGraph(1);
        selfLoop.addEdge(0, 0);
        rejected = false;
        try { new TopologicalSort().sort(selfLoop); }
        catch (IllegalStateException expected) { rejected = true; }
        check(rejected, "Self loop cycle");
        DirectedGraph chain = new DirectedGraph(20000);
        for (int i = 1; i < 20000; i++) { chain.addEdge(i - 1, i); }
        check(new Kosaraju().findSCC(chain).size() == 20000, "Iterative DFS deep graph");
    }

    private static void verifyScc(DirectedGraph graph) {
        int count = graph.getVertexCount();
        boolean[][] reachable = new boolean[count][count];
        for (int from = 0; from < count; from++) {
            reachable[from][from] = true;
            for (int to : graph.getNeighbors(from)) { reachable[from][to] = true; }
        }
        for (int k = 0; k < count; k++) {
            for (int i = 0; i < count; i++) {
                for (int j = 0; j < count; j++) {
                    reachable[i][j] |= reachable[i][k] && reachable[k][j];
                }
            }
        }
        int[] group = new int[count];
        Arrays.fill(group, -1);
        List<List<Integer>> groups = new Kosaraju().findSCC(graph);
        for (int index = 0; index < groups.size(); index++) {
            for (int vertex : groups.get(index)) {
                check(group[vertex] == -1, "Vertex belongs to one SCC");
                group[vertex] = index;
            }
        }
        for (int i = 0; i < count; i++) {
            check(group[i] != -1, "SCC covers every vertex");
            for (int j = 0; j < count; j++) {
                check((group[i] == group[j]) == (reachable[i][j] && reachable[j][i]),
                        "SCC matches mutual reachability");
            }
        }
    }

    private static void testRandomGraphs() {
        Random random = new Random(42);
        for (int run = 0; run < 30; run++) {
            Graph graph = new Graph(6);
            long infinity = Long.MAX_VALUE / 4;
            long[][] distances = new long[6][6];
            for (int i = 0; i < 6; i++) {
                Arrays.fill(distances[i], infinity); distances[i][i] = 0;
            }
            for (int i = 0; i < 6; i++) {
                for (int j = i + 1; j < 6; j++) {
                    if (random.nextBoolean()) {
                        int weight = random.nextInt(8);
                        graph.addEdge(i, j, weight);
                        distances[i][j] = weight; distances[j][i] = weight;
                    }
                }
            }
            for (int k = 0; k < 6; k++) {
                for (int i = 0; i < 6; i++) {
                    for (int j = 0; j < 6; j++) {
                        distances[i][j] = Math.min(distances[i][j],
                                distances[i][k] + distances[k][j]);
                    }
                }
            }
            for (int source = 0; source < 6; source++) {
                Dijkstra.Result result = new Dijkstra().shortestPaths(graph, source);
                for (int target = 0; target < 6; target++) {
                    long expected = distances[source][target] == infinity
                            ? Long.MAX_VALUE : distances[source][target];
                    check(result.distanceTo(target) == expected, "Dijkstra vs Floyd-Warshall");
                    List<Integer> path = result.pathTo(target);
                    if (expected == Long.MAX_VALUE) {
                        check(path.isEmpty(), "No invented unreachable path");
                    } else {
                        check(path.get(0) == source
                                && path.get(path.size() - 1) == target, "Path endpoints");
                        long pathWeight = 0;
                        for (int i = 1; i < path.size(); i++) {
                            long best = infinity;
                            for (Edge edge : graph.getNeighbors(path.get(i - 1))) {
                                if (edge.getTo() == path.get(i)) {
                                    best = Math.min(best, edge.getWeight());
                                }
                            }
                            check(best != infinity, "Path uses actual edges");
                            pathWeight += best;
                        }
                        check(pathWeight == expected, "Path weight matches distance");
                    }
                }
            }
            MstResult first = new Kruskal().findMST(graph);
            MstResult second = new Prim().findMST(graph);
            check(first.getTotalWeight() == second.getTotalWeight(), "Random MST agreement");
            check(first.getComponentCount() == second.getComponentCount(), "Random components");
            DirectedGraph directed = new DirectedGraph(6);
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 6; j++) {
                    if (random.nextInt(4) == 0) { directed.addEdge(i, j); }
                }
            }
            verifyScc(directed);
            DirectedGraph dag = GraphGenerator.directed(8, 15, run, true);
            validOrder(dag, new TopologicalSort().sort(dag));
        }
    }

    private static void testGenerator() {
        int[][] sizes = {{10, 30}, {100, 500}, {1000, 5000}};
        for (int[] size : sizes) {
            Graph first = GraphGenerator.connected(size[0], size[1], 123);
            Graph second = GraphGenerator.connected(size[0], size[1], 123);
            check(first.getEdges().size() == size[1], "Exact edge count");
            check(new Kruskal().findMST(first).isConnected(), "Generated connectivity");
            for (int i = 0; i < size[1]; i++) {
                check(first.getEdges().get(i).toString().equals(
                        second.getEdges().get(i).toString()), "Fixed seed reproducibility");
            }
            DirectedGraph dag = GraphGenerator.directed(size[0], size[1], 123, true);
            check(dag.getEdgeCount() == size[1], "Exact DAG edge count");
            validOrder(dag, new TopologicalSort().sort(dag));
            DirectedGraph cyclic = GraphGenerator.directed(size[0], size[1], 123, false);
            check(cyclic.getEdgeCount() == size[1], "Exact cyclic edge count");
        }
    }
}
