package com.riskrieg.mapeditor;

import com.riskrieg.mapeditor.map.Territory;
import com.riskrieg.mapeditor.util.ImageUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.MouseInputAdapter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Editor extends JFrame {

  private BufferedImage base;
  private BufferedImage text;

  private String mapName;

  private EditMode editMode;

  private JPanel imagePanel;
  private JPanel sidePanel;

  private Deque<Point> activePoints = new ArrayDeque<>(); // TODO: Model this.
  private final MapDataModel dataModel;

  private final DefaultListModel<Territory> territoryListModel;
  private JList<Territory> territoryJList;


  private static final int SIDE_BAR_WIDTH_PX = 100;
  private static final int WINDOW_WIDTH = 1280 + SIDE_BAR_WIDTH_PX;
  private static final int WINDOW_HEIGHT = 720 + (int) (((float) 720 / WINDOW_WIDTH) * SIDE_BAR_WIDTH_PX);

  public Editor() {
    dataModel = new MapDataModel();
    territoryListModel = new DefaultListModel<>();
    territoryJList = new JList<>(territoryListModel);
    territoryJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    editMode = EditMode.NO_EDIT;

    // Window generation
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setTitle(Constants.NAME + " Map Editor");

    setJMenuBar(menuBar());
    JPanel container = new JPanel();
    container.setLayout(new BorderLayout());
    container.add(sidePanel(), BorderLayout.WEST);
    container.add(mapPanel());
    this.add(container);

    pack();
    setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
    setResizable(true);
    setLocationRelativeTo(null);
  }

  public Editor(String mapName) {
    dataModel = new MapDataModel();
    territoryListModel = new DefaultListModel<>();
    territoryJList = new JList<>(territoryListModel);
    territoryJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    editMode = EditMode.ADD_TERRITORY;
    try {
      base = ImageIO.read(new File(Constants.MAP_PATH + mapName + "/" + mapName + "-base.png"));
      text = ImageIO.read(new File(Constants.MAP_PATH + mapName + "/" + mapName + "-text.png"));
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Window generation
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setTitle(Constants.NAME + " Map Editor");

    setJMenuBar(menuBar());
    JPanel container = new JPanel();
    container.setLayout(new BorderLayout());
    container.add(sidePanel(), BorderLayout.WEST);
    container.add(mapPanel());
    this.add(container);

    pack();
    GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    int screenWidth = gd.getDisplayMode().getWidth();
    int screenHeight = gd.getDisplayMode().getHeight();
    if (base.getWidth() < screenWidth || base.getHeight() < screenHeight) {
      setSize(base.getWidth() + 145, base.getHeight() + 75);
    } else {
      setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
    }
    setResizable(true);
    setLocationRelativeTo(null);

    rebuildMapPanel();
    rebuildSidePanel();
  }

  private JMenuBar menuBar() {
    // Creating the menu.
    JMenuBar menuBar = new JMenuBar();
    JMenu menuFile = new JMenu("File");
    JMenuItem miOpenBaseLayer = new JMenuItem(new AbstractAction("Open Base Layer...") {
      @Override
      public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Images", "png", "jpg", "jpeg");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          try {
            // TODO: Map name
            mapName = chooser.getSelectedFile().getName().replace(".png", "").replace("-base", "");
            base = ImageIO.read(chooser.getSelectedFile());
            rebuildMapPanel();
          } catch (IOException ex) {
            ex.printStackTrace();
          }
        }
      }
    });

    JMenuItem miOpenTextLayer = new JMenuItem(new AbstractAction("Open Text Layer...") {
      @Override
      public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Images", "png", "jpg", "jpeg");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          try {
            text = ImageIO.read(chooser.getSelectedFile());
            if (base == null) {
              text = null;
              JOptionPane.showMessageDialog(null, "You need to import a base map layer before importing your text layer.");
            } else if (text.getHeight() == base.getHeight() && text.getWidth() == base.getWidth()) {
              rebuildMapPanel();
            } else {
              text = null;
              JOptionPane.showMessageDialog(null, "Your text layer must match the width and height of your base layer.");
            }
          } catch (IOException ex) {
            ex.printStackTrace();
          }
        }
      }
    });

    JMenuItem miSave = new JMenuItem(new AbstractAction("Export to Json") {
      @Override
      public void actionPerformed(ActionEvent e) {
        // TODO: Save
        JTextArea fileNameArea = new JTextArea();
        fileNameArea.setEditable(true);

        String fileName = JOptionPane.showInputDialog(fileNameArea, "Enter map name:", mapName);
        if (fileName == null || fileName.isEmpty() || base == null || text == null) {
          JOptionPane.showMessageDialog(null, "Nothing to export.");
          return;
        }
        if (dataModel.save(fileName)) {
          JOptionPane.showMessageDialog(null, "Graph file saved in current directory.");
        } else {
          JOptionPane.showMessageDialog(null, "Error saving map file.");
        }
      }
    });

    JMenu menuEdit = new JMenu("Edit");
    JMenuItem modeAddTerritory = new JMenuItem(new AbstractAction("Mode: Add Territory") {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (base != null && text != null) {
          Editor.this.editMode = EditMode.ADD_TERRITORY;
          Editor.this.dataModel.clearSelection();
          rebuildSidePanel();
          rebuildMapPanel();
        } else {
          JOptionPane.showMessageDialog(null, "You need to import a base map layer and a text map layer before switching to an editing mode.");
        }
      }
    });

    JMenuItem modeAddNeighbors = new JMenuItem(new AbstractAction("Mode: Add Neighbor") {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (base != null && text != null) {
          Editor.this.editMode = EditMode.ADD_NEIGHBORS;
          for (Point point : Editor.this.activePoints) {
            ImageUtil.bucketFill(base, point, Constants.TERRITORY_COLOR);
          }
          Editor.this.activePoints.clear();
          rebuildSidePanel();
          rebuildMapPanel();
        } else {
          JOptionPane.showMessageDialog(null, "You need to import a base map layer and a text map layer before switching to an editing mode.");
        }
      }
    });

    menuFile.add(miOpenBaseLayer);
    menuFile.add(miOpenTextLayer);
    menuFile.add(miSave);
    menuEdit.add(modeAddTerritory);
    menuEdit.add(modeAddNeighbors);
    menuBar.add(menuFile);
    menuBar.add(menuEdit);
    return menuBar;
  }

  private JScrollPane mapPanel() {
    imagePanel = new JPanel();
    JScrollPane scrollPane = new JScrollPane(imagePanel);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
    return scrollPane;
  }

  private JPanel sidePanel() {
    sidePanel = new JPanel();
    sidePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2), "Territories", TitledBorder.CENTER, TitledBorder.CENTER));
    sidePanel.setLayout(new BorderLayout());

    JPanel buttonArea = new JPanel();
    buttonArea.setLayout(new GridLayout(2, 1));

    sidePanel.add(buttonArea, BorderLayout.NORTH);

    JScrollPane territoryScroll = new JScrollPane();
    territoryScroll.getVerticalScrollBar().setUnitIncrement(16);
    territoryScroll.setPreferredSize(new Dimension(SIDE_BAR_WIDTH_PX, this.getHeight()));
    territoryScroll.setViewportBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

    territoryScroll.setViewportView(territoryJList);
    sidePanel.add(territoryScroll, BorderLayout.CENTER);
    return sidePanel;
  }

  // Non-UI

  private void rebuildMapPanel() {
    imagePanel.removeAll();

    JLabel baseLabel = new JLabel();
    baseLabel.setLayout(new BorderLayout());

    dataModel.getSubmitted().forEach(submitted -> submitted.seedPoints().forEach(sp -> ImageUtil.bucketFill(base, sp.getLocation(), Constants.SUBMITTED_COLOR)));
    dataModel.getFinished().forEach(finished -> finished.seedPoints().forEach(sp -> ImageUtil.bucketFill(base, sp.getLocation(), Constants.FINISHED_COLOR)));
    dataModel.getSelectedNeighbors().forEach(sn -> sn.seedPoints().forEach(sp -> ImageUtil.bucketFill(base, sp.getLocation(), Constants.NEIGHBOR_SELECT_COLOR)));
    dataModel.getSelected().ifPresent(selected -> selected.seedPoints().forEach(sp -> ImageUtil.bucketFill(base, sp.getLocation(), Constants.SELECT_COLOR)));

    base = ImageUtil.convert(base, 2);
    baseLabel.setIcon(new ImageIcon(base));
    baseLabel.addMouseListener(mapClickListener());

    if (text != null) {
      JLabel textLabel = new JLabel();
      textLabel.setIcon(new ImageIcon(text));

      baseLabel.add(textLabel);
    }
    imagePanel.add(baseLabel);
    imagePanel.repaint();
    imagePanel.revalidate();
  }

  private void rebuildSidePanel() {
    sidePanel.removeAll();

    JPanel buttonArea = new JPanel();
    GridLayout buttonAreaLayout = new GridLayout(2, 1);
    buttonAreaLayout.setVgap(4);
    buttonArea.setLayout(buttonAreaLayout);
    buttonArea.setBorder(BorderFactory.createEmptyBorder(2, 2, 4, 2));

    switch (editMode) {
      case NO_EDIT -> {
        sidePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2), "Territories", TitledBorder.CENTER, TitledBorder.CENTER));
      }
      case ADD_TERRITORY -> {
        sidePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2), "Add Territories", TitledBorder.CENTER, TitledBorder.CENTER));

        JButton addTerritoryButton = new JButton("+");
        addTerritoryButton.setPreferredSize(new Dimension(10, 25));
        buttonArea.add(addTerritoryButton);
        addTerritoryButton.addMouseListener(addTerritoryButtonListener());

        JButton deleteTerritoryButton = new JButton("-");
        deleteTerritoryButton.setPreferredSize(new Dimension(10, 25));
        buttonArea.add(deleteTerritoryButton);
        deleteTerritoryButton.addMouseListener(removeTerritoryButtonListener());
      }
      case ADD_NEIGHBORS -> {
        sidePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2), "Neighbor Select", TitledBorder.CENTER, TitledBorder.CENTER));

        JButton submitNeighborButton = new JButton("Submit");
        submitNeighborButton.setPreferredSize(new Dimension(10, 25));
        buttonArea.add(submitNeighborButton);
        submitNeighborButton.addMouseListener(submitNeighborsButtonListener());
      }
    }

    sidePanel.add(buttonArea, BorderLayout.NORTH);

    JScrollPane territoryScroll = new JScrollPane();
    territoryScroll.getVerticalScrollBar().setUnitIncrement(16);
    territoryScroll.setPreferredSize(new Dimension(SIDE_BAR_WIDTH_PX, this.getHeight()));
    territoryScroll.setViewportBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

    territoryScroll.setViewportView(territoryJList);
    sidePanel.add(territoryScroll, BorderLayout.CENTER);

    sidePanel.repaint();
    sidePanel.revalidate();
  }

  private MouseInputAdapter mapClickListener() {
    return new MouseInputAdapter() {

      boolean pressed = false;

      @Override
      public void mousePressed(MouseEvent e) {
        pressed = true;
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if (pressed) {
          if (new Rectangle(e.getComponent().getLocationOnScreen(), e.getComponent().getSize()).contains(e.getLocationOnScreen())) {
            pressed = false;
            Point cursor = new Point(e.getX(), e.getY());
            switch (editMode) {
              case ADD_TERRITORY -> {
                if (ImageUtil.getPixelColor(base, cursor).equals(Constants.TERRITORY_COLOR)) {
                  if (e.getButton() == MouseEvent.BUTTON1) {
                    activePoints.add(ImageUtil.getRootPixel(base, cursor));
                    ImageUtil.bucketFill(base, cursor, Constants.SELECT_COLOR);
                  }
                } else if (ImageUtil.getPixelColor(base, cursor).equals(Constants.SELECT_COLOR)) {
                  if (e.getButton() == MouseEvent.BUTTON1) {
                    activePoints.remove(ImageUtil.getRootPixel(base, cursor));
                    ImageUtil.bucketFill(base, cursor, Constants.TERRITORY_COLOR);
                  }
                }
              }
              case ADD_NEIGHBORS -> {
                if (dataModel.getSelected().isPresent()) {
                  if (ImageUtil.getPixelColor(base, cursor).equals(Constants.SUBMITTED_COLOR) || ImageUtil.getPixelColor(base, cursor).equals(Constants.FINISHED_COLOR)) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                      getTerritory(cursor).ifPresent(dataModel::selectNeighbor);
                    }
                  } else if (ImageUtil.getPixelColor(base, cursor).equals(Constants.NEIGHBOR_SELECT_COLOR)) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                      getTerritory(cursor).ifPresent(dataModel::deselectNeighbor);
                    }
                  } else if (ImageUtil.getPixelColor(base, cursor).equals(Constants.SELECT_COLOR)) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                      dataModel.clearSelection();
                    }
                  }
                } else {
                  if (ImageUtil.getPixelColor(base, cursor).equals(Constants.SUBMITTED_COLOR)) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                      getTerritory(cursor).ifPresent(dataModel::select);
                    }
                  } else if (dataModel.getSelectedNeighbors().isEmpty() && ImageUtil.getPixelColor(base, cursor).equals(Constants.FINISHED_COLOR)) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                      getTerritory(cursor).ifPresent(dataModel::select);
                    }
                  }
                }
              }
              case NO_EDIT -> {
              }
            }
            rebuildMapPanel();
          }
        }
      }
    };
  }

  private MouseInputAdapter addTerritoryButtonListener() {
    return new MouseInputAdapter() {

      boolean pressed = false;

      @Override
      public void mousePressed(MouseEvent e) {
        pressed = true;
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if (pressed) {
          if (new Rectangle(e.getComponent().getLocationOnScreen(), e.getComponent().getSize()).contains(e.getLocationOnScreen())) {
            pressed = false;
            JTextArea nameArea = new JTextArea();
            nameArea.setEditable(true);

            String name = JOptionPane.showInputDialog(nameArea, "Enter territory name:");
            if (name == null || name.isEmpty()) {
              JOptionPane.showMessageDialog(null, "You did not enter a name so no changes were made.");
              return;
            }
            if (activePoints.isEmpty()) {
              JOptionPane.showMessageDialog(null, "You need to select a region or set of regions to constitute a territory.");
              return;
            }
            Territory newlySubmitted = new Territory(name, new HashSet<>(activePoints));
            territoryListModel.addElement(newlySubmitted);
            dataModel.submitTerritory(newlySubmitted);
            activePoints.clear();
            rebuildMapPanel();
          }
        }
      }
    };
  }

  private MouseInputAdapter removeTerritoryButtonListener() {
    return new MouseInputAdapter() {

      boolean pressed = false;

      @Override
      public void mousePressed(MouseEvent e) {
        pressed = true;
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if (pressed) {
          if (new Rectangle(e.getComponent().getLocationOnScreen(), e.getComponent().getSize()).contains(e.getLocationOnScreen())) {
            pressed = false;
            Territory selected = territoryJList.getSelectedValue();
            if (selected == null) {
              JOptionPane.showMessageDialog(null, "You need to select a territory to remove from the list.");
              return;
            }
            for (Point point : selected.seedPoints()) {
              ImageUtil.bucketFill(base, point, Constants.TERRITORY_COLOR);
            }
            territoryListModel.removeElement(selected);
            dataModel.removeSubmittedTerritory(selected);
            rebuildMapPanel();
          }
        }
      }
    };
  }

  private MouseInputAdapter submitNeighborsButtonListener() {
    return new MouseInputAdapter() {

      boolean pressed = false;

      @Override
      public void mousePressed(MouseEvent e) {
        pressed = true;
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if (pressed) {
          if (new Rectangle(e.getComponent().getLocationOnScreen(), e.getComponent().getSize()).contains(e.getLocationOnScreen())) {
            pressed = false;
            if (dataModel.getSelected().isPresent() && !dataModel.getSelectedNeighbors().isEmpty()) {
              dataModel.submitNeighbors();
            }
            rebuildMapPanel();
          }
        }
      }
    };
  }

  // Utility

  private Optional<Territory> getTerritory(Point point) {
    Point rootPoint = ImageUtil.getRootPixel(base, point);
    for (Territory territory : dataModel.getSubmitted()) {
      if (territory.seedPoints().contains(rootPoint)) {
        return Optional.of(territory);
      }
    }
    return Optional.empty();
  }

}
