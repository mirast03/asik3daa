package scc;

import graph.DirectedGraph;
import metrics.Metrics;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class Kosaraju {
    private static class Frame {
        final int vertex;
        int nextIndex;
        Frame(int vertex) { this.vertex = vertex; }
    }

    public List<List<Integer>> findSCC(DirectedGraph graph) {
        return findSCC(graph, new Metrics());
    }

    public List<List<Integer>> findSCC(DirectedGraph graph, Metrics metrics) {
        int count = graph.getVertexCount();
        boolean[] visited = new boolean[count];
        List<Integer> order = new ArrayList<>();
        Deque<Frame> stack = new ArrayDeque<>();

        for (int start = 0; start < count; start++) {
            if (visited[start]) { continue; }
            visited[start] = true;
            stack.push(new Frame(start));

            while (!stack.isEmpty()) {
                Frame frame = stack.peek();
                List<Integer> neighbors = graph.getNeighbors(frame.vertex);
                if (frame.nextIndex < neighbors.size()) {
                    int next = neighbors.get(frame.nextIndex++);
                    metrics.edgeExaminations++;
                    if (!visited[next]) {
                        visited[next] = true;
                        stack.push(new Frame(next));
                    }
                } else {
                    order.add(frame.vertex);
                    stack.pop();
                }
            }
        }

        DirectedGraph reversed = new DirectedGraph(count);
        for (int from = 0; from < count; from++) {
            for (int to : graph.getNeighbors(from)) {
                metrics.edgeExaminations++;
                reversed.addEdge(to, from);
            }
        }

        visited = new boolean[count];
        List<List<Integer>> components = new ArrayList<>();
        Deque<Integer> pending = new ArrayDeque<>();
        for (int i = order.size() - 1; i >= 0; i--) {
            int start = order.get(i);
            if (visited[start]) { continue; }
            List<Integer> component = new ArrayList<>();
            visited[start] = true;
            pending.push(start);
            while (!pending.isEmpty()) {
                int vertex = pending.pop();
                component.add(vertex);
                for (int next : reversed.getNeighbors(vertex)) {
                    metrics.edgeExaminations++;
                    if (!visited[next]) {
                        visited[next] = true;
                        pending.push(next);
                    }
                }
            }
            components.add(component);
        }
        return components;
    }
}
