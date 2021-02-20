package com.riskrieg.mapeditor.map.territory;

import java.awt.Point;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Territory implements Comparable<Territory> {

  private final String name;
  private final Set<Point> seedPoints;

  public Territory(String name, Set<Point> seedPoints) {
    this.name = name;
    this.seedPoints = seedPoints;
  }

  public Territory(String name, Point seedPoint) {
    this.name = name;
    this.seedPoints = new HashSet<>();
    this.seedPoints.add(seedPoint);
  }

  public String getName() {
    return name;
  }

  public Set<Point> getSeedPoints() {
    return seedPoints;
  }

  @Override
  public int compareTo(Territory o) {
    return this.getName().compareTo(o.getName());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Territory territory = (Territory) o;
    return Objects.equals(name, territory.name) &&
        Objects.equals(seedPoints, territory.seedPoints);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, seedPoints);
  }

  @Override
  public String toString() {
    return name;
  }

}
