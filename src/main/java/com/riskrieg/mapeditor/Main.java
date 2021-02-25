package com.riskrieg.mapeditor;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Main {

  public static void main(String[] args) {
    FlatLightLaf.install();

    SwingUtilities.invokeLater(() -> {
//      try {
//        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
//        ex.printStackTrace();
//      }
      try {
        Editor editor = new Editor("sea-of-japan");
        editor.setVisible(true);
      } catch (Exception e) {
        System.exit(0);
      }
    });
  }

}
