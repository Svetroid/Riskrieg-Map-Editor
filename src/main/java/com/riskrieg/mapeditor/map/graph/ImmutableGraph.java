package com.riskrieg.mapeditor.map.graph;

import java.util.HashSet;

public class ImmutableGraph<T> extends Graph<T> {

  public ImmutableGraph(HashSet<T> nodes, HashSet<Edge<T>> edges) {
    super(nodes, edges);
  }

}
