package shortestpath;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class Dijkstra {
    private static class State {
        final int vertex;
        final long distance;
        State(int vertex, long distance) {
            this.vertex = vertex;
            this.distance = distance;
        }
    }

    public static class Result {
        private final long[] distance;
        private final int[] previous;
        private final int source;

        private Result(long[] distance, int[] previous, int source) {
            this.distance = distance;
            this.previous = previous;
            this.source = source;
        }

        public long distanceTo(int vertex) { return distance[vertex]; }
        public boolean isReachable(int vertex) {
            return distance[vertex] != Long.MAX_VALUE;
        }

        public List<Integer> pathTo(int target) {
            if (!isReachable(target)) { return Collections.emptyList(); }
            List<Integer> path = new ArrayList<>();
            for (int vertex = target; vertex != -1;
                 vertex = previous[vertex]) {
                path.add(vertex);
                if (vertex == source) { break; }
            }
            Collections.reverse(path);
            return path;
        }
    }

    public Result shortestPaths(Graph graph, int source) {
        return shortestPaths(graph, source, new Metrics());
    }

    public Result shortestPaths(Graph graph, int source, Metrics metrics) {
        int count = graph.getVertexCount();
        if (source < 0 || source >= count) {
            throw new IllegalArgumentException("Invalid source vertex");
        }
        for (Edge road : graph.getEdges()) {
            metrics.edgeExaminations++;
            if (road.getWeight() < 0) {
                throw new IllegalArgumentException(
                        "Dijkstra requires non-negative weights");
            }
        }

        long[] distance = new long[count];
        int[] previous = new int[count];
        Arrays.fill(distance, Long.MAX_VALUE);
        Arrays.fill(previous, -1);
        distance[source] = 0;

        PriorityQueue<State> queue = new PriorityQueue<>((first, second) -> {
            metrics.comparisons++;
            return Long.compare(first.distance, second.distance);
        });
        queue.add(new State(source, 0));
        metrics.queueOffers++;

        while (!queue.isEmpty()) {
            State current = queue.remove();
            metrics.queuePolls++;
            if (current.distance != distance[current.vertex]) { continue; }

            for (Edge road : graph.getNeighbors(current.vertex)) {
                metrics.edgeExaminations++;
                int next = road.getTo();
                long candidate = current.distance + road.getWeight();
                if (candidate < distance[next]) {
                    distance[next] = candidate;
                    previous[next] = current.vertex;
                    queue.add(new State(next, candidate));
                    metrics.queueOffers++;
                }
            }
        }
        return new Result(distance, previous, source);
    }
}
