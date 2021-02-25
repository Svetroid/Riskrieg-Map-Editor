package com.riskrieg.mapeditor;

import com.riskrieg.mapeditor.map.graph.Edge;
import com.riskrieg.mapeditor.map.graph.MutableGraph;
import com.riskrieg.mapeditor.map.territory.Territory;
import com.riskrieg.mapeditor.util.GsonUtil;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class MapDataModel {

  private MutableGraph<Territory> graph;

  private Territory selected;
  private Set<Territory> selectedNeighbors;
  private Set<Territory> finishedTerritories;

  public MapDataModel() {
    graph = new MutableGraph<>();
    selectedNeighbors = new HashSet<>();
    finishedTerritories = new HashSet<>();
  }

  public boolean save(String fileName) {
    try {
      GsonUtil.saveToJson(graph, fileName + ".json");
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public Set<Territory> getSubmitted() {
    return graph.getNodes();
  }

  public Set<Territory> getFinished() {
    return finishedTerritories;
  }

  public Optional<Territory> getSelected() {
    return Optional.ofNullable(selected);
  }

  public Set<Territory> getSelectedNeighbors() {
    return selectedNeighbors;
  }

  public void select(Territory selected) {
    this.selected = selected;
    if (selected != null) {
      selectedNeighbors.addAll(graph.getNeighbors(selected));
    }
  }

  public void clearSelection() {
    this.selected = null;
    this.selectedNeighbors.clear();
  }

  public boolean selectNeighbor(Territory territory) {
    if (territory == null || territory.equals(selected)) {
      return false;
    }
    return this.selectedNeighbors.add(territory);
  }

  public boolean deselectNeighbor(Territory territory) {
    return this.selectedNeighbors.remove(territory);
  }

  public void submitTerritory(Territory territory) {
    graph.addNode(territory);
  }

  public boolean removeSubmittedTerritory(Territory territory) {
    finishedTerritories.remove(territory);
    return graph.removeNode(territory);
  }

  public void submitNeighbors() {
    // Remove deselected neighbors.
    if (graph.getNodes().contains(selected)) {
      Set<Edge<Territory>> edgesToRemove = new HashSet<>();
      Set<Territory> currentNeighbors = graph.getNeighbors(selected);
      currentNeighbors.removeAll(selectedNeighbors); // This is the set of all deselected neighboring territories.
      for (Territory deselectedNeighbor : currentNeighbors) {
        for (Edge<Territory> edge : graph.getEdges()) {
          if (edge.equals(new Edge<>(selected, deselectedNeighbor))) {
            edgesToRemove.add(edge);
          }
        }
      }
      graph.removeEdges(edgesToRemove);
    }

    // Add all selected neighbors in case any new ones were added.
    for (Territory selectedNeighbor : selectedNeighbors) {
      graph.putEdge(selected, selectedNeighbor);
    }
    finishedTerritories.add(selected);
    clearSelection();
  }

}
