package com.riskrieg.mapeditor.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GsonUtil {

  private GsonUtil() {
  }

  private static Gson createGson() {
    return new GsonBuilder()
        .setPrettyPrinting()
        .create();
  }

  public static Object loadFromJson(String file, Class classType) {
    Object result = null;
    if (new File(file).exists()) {
      try {
        Gson gson = createGson();
        Reader reader = new FileReader(file, StandardCharsets.UTF_8);
        JsonReader jReader = new JsonReader(reader);
        jReader.setLenient(true);
        try {
          result = gson.fromJson(jReader, classType);
        } catch (Exception e) {
          System.out.println(file);
          e.printStackTrace();
        }
        reader.close();
        return result;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return result;
  }

  public static Object loadFromJson(String file, Type type) {
    Object result = null;
    if (new File(file).exists()) {
      try {
        Gson gson = createGson();
        Reader reader = new FileReader(file, StandardCharsets.UTF_8);
        JsonReader jReader = new JsonReader(reader);
        jReader.setLenient(true);
        result = gson.fromJson(jReader, type);
        reader.close();
        return result;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return result;
  }

  public static void saveToJson(Object object, String file) {
    Gson gson = createGson();
    try {
      Writer writer = new FileWriter(file, StandardCharsets.UTF_8);
      writer.write(gson.toJson(object));
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void saveToJson(Object object, String file, Type type) {
    Gson gson = createGson();
    try {
      Writer writer = new FileWriter(file, StandardCharsets.UTF_8);
      writer.write(gson.toJson(object, type));
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void saveToJson(Object object, String directory, String fileName) {
    Gson gson = createGson();
    try {
      Files.createDirectories(Paths.get(directory));
      Writer writer = new FileWriter(directory + fileName, StandardCharsets.UTF_8);
      writer.write(gson.toJson(object));
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void saveToJson(Object object, String directory, String fileName, Type type) {
    Gson gson = createGson();
    try {
      Files.createDirectories(Paths.get(directory));
      Writer writer = new FileWriter(directory + fileName, StandardCharsets.UTF_8);
      writer.write(gson.toJson(object, type));
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
