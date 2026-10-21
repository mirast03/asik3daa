package bench;

import graph.DirectedGraph;
import graph.Graph;
import metrics.Metrics;
import mst.Kruskal;
import mst.MstResult;
import mst.Prim;
import scc.Kosaraju;
import shortestpath.Dijkstra;
import topo.TopologicalSort;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class Benchmark {
    private static final long SEED = 20261019L;
    private static final int REPEATS = 5;
    private static final int WARMUPS = 20;
    private static volatile long sink;

    private interface Trial { long run(Metrics metrics); }

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.ROOT);
        Files.createDirectories(Path.of("results/tables"));
        try (PrintWriter raw = new PrintWriter("results/tables/raw.csv");
             PrintWriter summary = new PrintWriter("results/tables/summary.csv")) {
            String counters = "edgeExaminations,comparisons,findCalls,unionCalls,"
                    + "queueOffers,queuePolls";
            raw.println("algorithm,graph,V,E,seed,run,timeNs," + counters);
            summary.println("algorithm,graph,V,E,seed,repeats,meanNs,minNs,maxNs," + counters);
            int[][] sizes = {{10, 30}, {100, 500}, {1000, 5000}};
            for (int[] size : sizes) {
                int vertices = size[0];
                int edges = size[1];
                long seed = SEED + vertices;
                Graph graph = GraphGenerator.connected(vertices, edges, seed);
                DirectedGraph dag = GraphGenerator.directed(vertices, edges, seed, true);
                DirectedGraph cyclic = GraphGenerator.directed(vertices, edges, seed, false);

                MstResult first = new Kruskal().findMST(graph);
                MstResult second = new Prim().findMST(graph);
                if (!first.isConnected() || !second.isConnected()
                        || first.getTotalWeight() != second.getTotalWeight()) {
                    throw new AssertionError("MST comparison failed");
                }
                measure("Kruskal", "connected", vertices, edges, seed,
                        m -> new Kruskal().findMST(graph, m).getTotalWeight(), raw, summary);
                measure("Prim", "connected", vertices, edges, seed,
                        m -> new Prim().findMST(graph, m).getTotalWeight(), raw, summary);
                measure("Dijkstra", "connected", vertices, edges, seed,
                        m -> new Dijkstra().shortestPaths(graph, 0, m)
                                .distanceTo(vertices - 1), raw, summary);
                measure("Kosaraju", "cyclic", vertices, edges, seed,
                        m -> new Kosaraju().findSCC(cyclic, m).size(), raw, summary);
                measure("Topo-DAG", "dag", vertices, edges, seed,
                        m -> new TopologicalSort().sort(dag, m).size(), raw, summary);
                measure("Topo-cycle", "cyclic", vertices, edges, seed,
                        m -> detectCycle(cyclic, m), raw, summary);
            }
        }
        try (PrintWriter environment = new PrintWriter("results/tables/environment.txt")) {
            environment.println("Measured at: " + java.time.Instant.now());
            environment.println("Java: " + System.getProperty("java.version"));
            environment.println("VM: " + System.getProperty("java.vm.name"));
            environment.println("OS: " + System.getProperty("os.name") + " "
                    + System.getProperty("os.version") + " " + System.getProperty("os.arch"));
            environment.println("Available processors: "
                    + Runtime.getRuntime().availableProcessors());
            environment.println("Warmups per case: " + WARMUPS);
            environment.println("Measured repeats per case: " + REPEATS);
        }
        System.out.println("Saved raw.csv and summary.csv: 18 cases, 90 measured runs.");
    }

    private static long detectCycle(DirectedGraph graph, Metrics metrics) {
        try {
            new TopologicalSort().sort(graph, metrics);
        } catch (IllegalStateException expected) {
            return -1;
        }
        throw new AssertionError("Cycle was not detected");
    }

    private static void measure(String name, String kind, int vertices, int edges,
                                long seed, Trial trial, PrintWriter raw,
                                PrintWriter summary) {
        for (int i = 0; i < WARMUPS; i++) { sink = trial.run(new Metrics()); }
        long total = 0;
        long minimum = Long.MAX_VALUE;
        long maximum = 0;
        double[] sums = new double[6];

        for (int run = 1; run <= REPEATS; run++) {
            Metrics metrics = new Metrics();
            long started = System.nanoTime();
            long value = trial.run(metrics);
            long elapsed = System.nanoTime() - started;
            sink = value;
            total += elapsed;
            minimum = Math.min(minimum, elapsed);
            maximum = Math.max(maximum, elapsed);
            long[] counts = {metrics.edgeExaminations, metrics.comparisons,
                    metrics.findCalls, metrics.unionCalls,
                    metrics.queueOffers, metrics.queuePolls};
            raw.printf("%s,%s,%d,%d,%d,%d,%d", name, kind, vertices, edges,
                    seed, run, elapsed);
            for (int i = 0; i < counts.length; i++) {
                raw.print("," + counts[i]);
                sums[i] += counts[i];
            }
            raw.println();
        }

        summary.printf("%s,%s,%d,%d,%d,%d,%.1f,%d,%d", name, kind,
                vertices, edges, seed, REPEATS, (double) total / REPEATS,
                minimum, maximum);
        for (double sum : sums) { summary.printf(",%.1f", sum / REPEATS); }
        summary.println();
    }
}
