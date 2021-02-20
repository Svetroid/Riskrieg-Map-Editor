package com.riskrieg.mapeditor.map.graph;

public class Edge<T> {

  private final T source;
  private final T target;

  public Edge(T source, T target) {
    this.source = source;
    this.target = target;
  }

  public T getSource() {
    return source;
  }

  public T getTarget() {
    return target;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Edge<?> edge = (Edge<?>) o;
    return (source.equals(edge.source) && target.equals(edge.target)) || (target.equals(edge.source) && source.equals(edge.target));
  }

  @Override
  public int hashCode() {
    int hash = 17;
    int hashMultiplier = 79;
    int hashSum = source.hashCode() + target.hashCode();
    hash = hashMultiplier * hash * hashSum;
    return hash;
  }

}
