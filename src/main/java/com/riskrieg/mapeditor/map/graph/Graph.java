package com.riskrieg.mapeditor.map.graph;

import java.util.HashSet;
import java.util.Set;

public abstract class Graph<T> {

  protected final HashSet<T> nodes;
  protected final HashSet<Edge<T>> edges;

  public Graph(HashSet<T> nodes, HashSet<Edge<T>> edges) {
    this.nodes = nodes;
    this.edges = edges;
  }

  public HashSet<T> getNodes() {
    return nodes;
  }

  public HashSet<Edge<T>> getEdges() {
    return edges;
  }

  public Set<T> getNeighbors(T source) {
    Set<T> result = new HashSet<>();
    for (T node : nodes) {
      if (neighbors(node, source)) {
        result.add(node);
      }
    }
    return result;
  }

  public boolean neighbors(T source, T target) {
    return edges.contains(new Edge<>(source, target));
  }

}
