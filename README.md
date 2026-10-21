

Readme · MD
# Smart City Infrastructure and Route Planning

Assignment 3 for Design and Analysis of Algorithms. This Java program treats city districts as vertices and roads as weighted edges. It answers four questions:

- What is the cheapest set of roads that connects every district? (Kruskal and Prim)
- In what order can services be set up when some depend on others? (topological sort)
- Which districts can reach each other through one-way streets? (Kosaraju SCC)
- What is the shortest route from one district to all the others? (Dijkstra)
## Quick start

You need JDK 17 or newer. Python 3.8 or newer is only needed for the report and plots. There are no external packages.

From the repository root on macOS or Linux:

```bash
mkdir -p out
find src -name '*.java' > /tmp/asik3daa-sources.txt
javac --release 17 -encoding UTF-8 -d out @/tmp/asik3daa-sources.txt
java -cp out Main
java -cp out tests.AlgorithmTests
java -cp out bench.Benchmark
python3 scripts/report.py
```

In IntelliJ, open the project, pick JDK 17+, mark `src` as Sources Root, and run `main` in `Main`, `AlgorithmTests` or `Benchmark`. For `Benchmark`, set the working directory to the project root, otherwise the results end up in the wrong place. After a new benchmark run, `scripts/report.py` refreshes the tables, plots and this README.

## Graph models

`Graph` is an undirected weighted graph. Each road is stored once in the edge list and twice in the adjacency lists, once per direction. `DirectedGraph` stores one-way arrows only and never adds the reverse arrow by itself. Vertices are numbered 0 to V-1, and isolated vertices are allowed.

## Algorithms

- DSU (union-find) tracks which districts are already connected. `find` uses path compression, and `union` attaches the smaller tree to the larger one.
- Kruskal sorts a copy of the edge list and takes a road only if its two ends are in different DSU groups.
- Prim grows a tree from a start district using Java's `PriorityQueue`. Heap entries whose far end is already visited are skipped. It restarts from every unvisited district, so disconnected graphs work too.
- Both MST algorithms return a minimum spanning forest, its total weight (as a `long`) and the number of components. A connected graph has one component and V-1 selected roads. A single vertex counts as connected with weight 0. An empty graph has zero components and counts as not connected.
- Kahn's topological sort counts incoming arrows for each vertex and repeatedly removes vertices that have none. If fewer than V vertices get removed, the graph has a cycle and the sort throws an exception. Several valid orders can exist.
- Kosaraju finds strongly connected components in three steps: a DFS that records finishing order, reversing all arrows, and a second DFS in reverse finishing order. Both DFS passes use explicit stacks, so a long chain of districts cannot overflow Java's call stack. The order of districts inside a component has no meaning.
- Dijkstra uses adjacency lists and a binary heap. Every time a distance improves, a new heap entry is added, and outdated entries are skipped when they come out. It returns distances and predecessors, which are enough to rebuild any route. An unreachable district has distance `Long.MAX_VALUE` and an empty path. The source has distance 0 and path `[source]`.
  Dijkstra rejects negative weights before it starts. Its proof assumes the smallest tentative distance is already final, and a negative edge can break that. In an undirected graph a negative road also creates a negative cycle (walk back and forth along it), so shortest distances are undefined. Directed graphs with negative edges need Bellman-Ford. Kruskal and Prim accept negative weights.

## Complexity

E is the number of roads, so an undirected adjacency list holds 2E entries. Space excludes the input graph itself.

| Algorithm | Time | Extra space |
|---|---|---|
| DSU build | O(V) | O(V) |
| DSU find / union | O(alpha(V)) amortized | O(V) in total |
| Kruskal | O(V + E log E) | O(V + E) |
| Lazy Prim | O(V + E log E) | O(V + E) |
| Kahn topological sort | O(V + E) | O(V) |
| Kosaraju | O(V + E) | O(V + E) |
| Dijkstra with lazy heap | O(V + E log E) | O(V + E) |
| Rebuild one route | O(V) | O(V) |

For a connected graph, E log E is the same as O(E log V), which is how these bounds are often written. The V term appears because arrays are initialized even for isolated vertices.

## Experiment setup

| Configuration | V | E |
|---|---:|---:|
| Small | 10 | 30 |
| Medium | 100 | 500 |
| Large | 1000 | 5000 |

Graphs:

- Each size uses seed `20261019 + V`. Weights are integers from 1 to 100, so ties are common. There are no self loops or duplicate roads.
- Connected graphs: every vertex i > 0 first joins a random earlier vertex, which guarantees connectivity. Random unused pairs are then added until there are E roads. Kruskal, Prim and Dijkstra run on the same graph at each size.
- DAGs: arrows only go from a smaller ID to a larger one, so cycles are impossible.
- Cyclic directed graphs: they start with 0->1 and 1->0, then get random distinct arrows up to E. They are used for Kosaraju and for cycle detection in the topological sort.
- Generation is reproducible but makes no claim to be uniform over all graphs.
  Timing:

- There are 6 cases per size (Kruskal, Prim, Dijkstra, Kosaraju, Topo-DAG, Topo-cycle). Each gets 20 untimed warmup calls and then exactly 5 measured calls, 90 measured runs in total.
- `System.nanoTime()` wraps the whole algorithm call, including its allocations and counters. Graph generation, printing and file output happen outside the timed part.
- Topo-cycle runs include throwing and catching the expected exception.
- Mean, min and max are recorded, and `raw.csv` keeps all 90 runs. The same graph is reused for the 5 runs, so the spread shows timing noise and says nothing about different random graphs.
  Counters:

- `edgeExaminations`: how often an algorithm looks at a road. Kruskal counts sorted roads. Prim counts adjacency entries plus heap polls. Dijkstra counts the weight check plus non-stale adjacency entries. Kahn counts the indegree pass and the processing pass. Kosaraju counts all three passes, including the reversal.
- `comparisons`: calls to the sort or heap comparator.
- `findCalls` and `unionCalls`: DSU calls, recursive `find` calls included.
- `queueOffers` and `queuePolls`: insertions and removals in Prim, Dijkstra and Kahn. DFS stack operations are not counted.
  A zero means the algorithm never does that operation. The counters are defined per algorithm, so they should not be added into one cost score.

Environment of the committed results:

```text
Measured at: 2026-10-19T06:14:53.619011Z
Java: 25.0.4.1
VM: OpenJDK 64-Bit Server VM
OS: Mac OS X 27.0.1 aarch64
Available processors: 10
Warmups per case: 20
Measured repeats per case: 5
```

## Results

These are real measurements from `Benchmark`, not estimates. A new run gives different times. Graphs and operation counts stay the same for the same seed and code.

| Algorithm | V | E | Mean (ms) | Min (ms) | Max (ms) |
|---|---:|---:|---:|---:|---:|
| Kruskal | 10 | 30 | 0.016400 | 0.014042 | 0.020542 |
| Prim | 10 | 30 | 0.022275 | 0.018875 | 0.024541 |
| Dijkstra | 10 | 30 | 0.011883 | 0.009375 | 0.016500 |
| Kosaraju | 10 | 30 | 0.021183 | 0.018958 | 0.023625 |
| Topo-DAG | 10 | 30 | 0.006800 | 0.006291 | 0.007375 |
| Topo-cycle | 10 | 30 | 0.003966 | 0.003083 | 0.004542 |
| Kruskal | 100 | 500 | 0.084192 | 0.079208 | 0.090625 |
| Prim | 100 | 500 | 0.095267 | 0.092625 | 0.096875 |
| Dijkstra | 100 | 500 | 0.149117 | 0.147791 | 0.150917 |
| Kosaraju | 100 | 500 | 0.193117 | 0.190000 | 0.200625 |
| Topo-DAG | 100 | 500 | 0.051792 | 0.050209 | 0.052958 |
| Topo-cycle | 100 | 500 | 0.023208 | 0.020708 | 0.029042 |
| Kruskal | 1000 | 5000 | 0.454142 | 0.419500 | 0.487625 |
| Prim | 1000 | 5000 | 0.477742 | 0.442875 | 0.548500 |
| Dijkstra | 1000 | 5000 | 0.276783 | 0.260708 | 0.295083 |
| Kosaraju | 1000 | 5000 | 0.357783 | 0.346000 | 0.378250 |
| Topo-DAG | 1000 | 5000 | 0.151425 | 0.148292 | 0.159708 |
| Topo-cycle | 1000 | 5000 | 0.066525 | 0.060459 | 0.070750 |

Operation counts (mean per run):

| Algorithm | V | Edge examinations | Key comparisons | Find calls | Union calls | Queue offers | Queue polls |
|---|---:|---:|---:|---:|---:|---:|---:|
| Kruskal | 10 | 30 | 113 | 129 | 9 | 0 | 0 |
| Prim | 10 | 90 | 198 | 0 | 0 | 30 | 30 |
| Dijkstra | 10 | 90 | 83 | 0 | 0 | 17 | 17 |
| Kosaraju | 10 | 90 | 0 | 0 | 0 | 0 | 0 |
| Topo-DAG | 10 | 60 | 0 | 0 | 0 | 10 | 10 |
| Topo-cycle | 10 | 30 | 0 | 0 | 0 | 0 | 0 |
| Kruskal | 100 | 500 | 3837 | 2194 | 99 | 0 | 0 |
| Prim | 100 | 1500 | 7735 | 0 | 0 | 500 | 500 |
| Dijkstra | 100 | 1500 | 2204 | 0 | 0 | 190 | 190 |
| Kosaraju | 100 | 1500 | 0 | 0 | 0 | 0 | 0 |
| Topo-DAG | 100 | 1000 | 0 | 0 | 0 | 100 | 100 |
| Topo-cycle | 100 | 502 | 0 | 0 | 0 | 1 | 1 |
| Kruskal | 1000 | 5000 | 50323 | 22248 | 999 | 0 | 0 |
| Prim | 1000 | 15000 | 111018 | 0 | 0 | 5000 | 5000 |
| Dijkstra | 1000 | 15000 | 35250 | 0 | 0 | 1925 | 1925 |
| Kosaraju | 1000 | 15000 | 0 | 0 | 0 | 0 | 0 |
| Topo-DAG | 1000 | 10000 | 0 | 0 | 0 | 1000 | 1000 |
| Topo-cycle | 1000 | 5026 | 0 | 0 | 0 | 4 | 4 |

![Снимок экрана — 2026-10-21 в 20.42.11.png](src/%D0%A1%D0%BD%D0%B8%D0%BC%D0%BE%D0%BA%20%D1%8D%D0%BA%D1%80%D0%B0%D0%BD%D0%B0%C2%A0%E2%80%94%202026-10-21%20%D0%B2%C2%A020.42.11.png)

![Снимок экрана — 2026-10-21 в 20.42.23.png](src/%D0%A1%D0%BD%D0%B8%D0%BC%D0%BE%D0%BA%20%D1%8D%D0%BA%D1%80%D0%B0%D0%BD%D0%B0%C2%A0%E2%80%94%202026-10-21%20%D0%B2%C2%A020.42.23.png)

## What the results show

- Kruskal and Prim return the same MST weight on every benchmark graph.
- Lazy Prim pushes every adjacency entry into its heap, including entries whose far end is already visited. At V=1000 it makes about 2.2 times as many comparisons as Kruskal's sort (111018 against 50323).
- Dijkstra looks at 3E roads: E for the weight check and 2E adjacency entries. It queues far fewer entries than Prim (1925 against 5000 at V=1000), because only improved distances are pushed.
- Kosaraju also looks at 3E roads: two DFS passes and the reversal. Topo-DAG looks at 2E.
- Topo-cycle is faster than Topo-DAG because it stops after a few vertices, but it still reads every road once to count indegrees.
- Time does not grow in step with operation counts. At well under a millisecond per run, JIT compilation, allocation, caches and GC all matter.
  Limits to keep in mind:

- V and E grow together in the three required sizes, and both plots use the same runs, so they cannot separate the effect of V from the effect of E.
- Three sizes and five runs per case are not much data, and warmups do not guarantee a fully settled JIT.
- Any time ranking holds only for these inputs, this code and this machine.
  On DSU: path compression rewrites parents to point at the root after a `find`. Union by size stops a large tree from being hung under a single vertex. Without them, a chain of bad unions makes `find` linear. Size balancing alone bounds tree height by O(log V), and together with compression the amortized cost per operation is inverse-Ackermann. The DSU test counts recursive `find` calls before and after compressing a real two-link path.

## Tests

`tests.AlgorithmTests` throws `AssertionError` on any failed check, even when JVM assertions are off. It covers:

- The small example city: MST weight 6, distances from 0 are [0, 2, 5, 6], and the path to 3 is [0, 1, 2, 3].
- Disconnected graphs, isolated vertices, zero vertices, one vertex, parallel roads and a self loop. Forests must stay acyclic.
- Several valid MSTs and repeated weights, compared by weight and validity instead of exact edges.
- Dijkstra edge cases: unreachable routes, source to source, zero weights, distances above `Integer.MAX_VALUE`, and rejection of negative weights.
- DSU: union by size, repeated union, and fewer recursive `find` calls after compression.
- Topological sort: every arrow must point forward in the result, and cycles and self loops must be rejected.
- SCC results compared with independent reachability on small graphs.
- Thirty fixed-seed random graphs: Dijkstra against Floyd-Warshall from every source, rebuilt paths using real edges with the reported cost, and Kruskal against Prim. Also thirty random directed graphs for SCC and thirty random DAGs for topological order.
- A 20,000-vertex chain to check the explicit DFS stack.
- Exact benchmark sizes, guaranteed connectivity and reproducibility from fixed seeds.
  A successful run prints a PASS count. These checks cover the required cases and do not prove correctness for every possible input.

## Project structure

| Path | Contents |
|---|---|
| `src/graph` | Undirected weighted and directed graphs |
| `src/dsu` | Union-find |
| `src/mst` | Kruskal, lazy Prim, forest result |
| `src/topo`, `src/scc` | Kahn ordering, Kosaraju SCC |
| `src/shortestpath` | Dijkstra with route rebuilding |
| `src/metrics` | Operation counters |
| `src/bench` | Seeded graph generators and timing runner |
| `src/tests` | Correctness checks |
| `results/tables` | Raw times, summary and environment |
| `results/plots` | The two SVG plots |
| `scripts` | Report template and plot/report generator |

The assignment's example `connectivity` package is split here into `topo` and `scc`.

## Conclusion

Kruskal and Prim plan the cheapest road network and return a forest when the city cannot be fully connected. Kahn's sort orders service dependencies and rejects cycles. Kosaraju groups districts that reach each other through one-way streets. Dijkstra gives shortest distances and routes as long as no road length is negative. The benchmark records time and algorithm-specific work side by side, and its short runs should be read with the limits listed above.



Claude finished the response

