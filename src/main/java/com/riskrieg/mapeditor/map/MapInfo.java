package com.riskrieg.mapeditor.map;

public class MapInfo {

  private final String name;
  private final String displayName;
  private final String author;
  private MapStatus status;

  public MapInfo(String name, String displayName, String author) {
    this.name = name;
    this.displayName = displayName;
    this.author = author;
  }

  public String getName() {
    return name;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getAuthor() {
    return author;
  }

  public MapStatus getStatus() {
    return status;
  }

  public void setStatus(MapStatus status) {
    this.status = status;
  }

}
