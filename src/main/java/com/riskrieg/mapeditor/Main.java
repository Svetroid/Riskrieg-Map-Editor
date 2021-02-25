package com.riskrieg.mapeditor;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.SwingUtilities;

public class Main {

  public static void main(String[] args) {
    FlatLightLaf.install();

    SwingUtilities.invokeLater(() -> {
      try {
        Editor editor = new Editor();
        editor.setVisible(true);
      } catch (Exception e) {
        System.exit(0);
      }
    });
  }

}
