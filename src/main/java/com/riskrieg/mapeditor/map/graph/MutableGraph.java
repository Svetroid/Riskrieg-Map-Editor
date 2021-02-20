package com.riskrieg.mapeditor.map.graph;

import java.util.HashSet;
import java.util.Set;

public class MutableGraph<T> extends Graph<T> {

  public MutableGraph() {
    super(new HashSet<>(), new HashSet<>());
  }

  public MutableGraph(HashSet<T> nodes, HashSet<Edge<T>> edges) {
    super(nodes, edges);
  }

  public ImmutableGraph<T> immutable() {
    return new ImmutableGraph<>(getNodes(), getEdges());
  }

  public boolean addNode(T node) {
    return nodes.add(node);
  }

  public boolean putEdge(T source, T target) {
    nodes.add(source);
    nodes.add(target);
    return edges.add(new Edge<>(source, target));
  }

  public boolean removeEdges(Set<Edge<T>> edgeSet) {
    return edges.removeAll(edgeSet);
  }

}
