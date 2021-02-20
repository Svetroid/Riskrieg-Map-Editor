package com.riskrieg.mapeditor;

import com.riskrieg.mapeditor.map.graph.Edge;
import com.riskrieg.mapeditor.map.graph.MutableGraph;
import com.riskrieg.mapeditor.map.territory.Territory;
import com.riskrieg.mapeditor.util.GsonUtil;
import com.riskrieg.mapeditor.util.ImageUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
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

  private EditMode editMode;

  private JPanel imagePanel;
  private JPanel sidePanel;

  private final DefaultListModel<Territory> territoryListModel;
  private JList<Territory> territoryJList;

  private Deque<Point> activePoints = new ArrayDeque<>();

  private Territory neighborModeSelected;
  private Set<Territory> neighbors = new HashSet<>();

  private MutableGraph<Territory> graph = new MutableGraph<>();
  private Set<Territory> finishedTerritories = new HashSet<>();

  private static int SIDE_BAR_WIDTH_PX = 100;
  private static int WINDOW_WIDTH = 1280 + SIDE_BAR_WIDTH_PX;
  private static int WINDOW_HEIGHT = 720 + (int) (((float) 720 / WINDOW_WIDTH) * SIDE_BAR_WIDTH_PX);

  public Editor() {
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

        String fileName = JOptionPane.showInputDialog(fileNameArea, "Enter map name:");
        if (fileName == null || fileName.isEmpty() || graph == null || base == null || text == null) {
          JOptionPane.showMessageDialog(null, "Nothing to export.");
          return;
        }
        GsonUtil.saveToJson(graph, fileName + ".json");
        JOptionPane.showMessageDialog(null, "Graph file saved in current directory.");
      }
    });

    JMenu menuEdit = new JMenu("Edit");
    JMenuItem modeAddTerritory = new JMenuItem(new AbstractAction("Add Territory Mode") {
      @Override
      public void actionPerformed(ActionEvent e) {
        Editor.this.editMode = EditMode.ADD_TERRITORY;
        rebuildSidePanel();
      }
    });

    JMenuItem modeAddNeighbors = new JMenuItem(new AbstractAction("Add Neighbor Mode") {
      @Override
      public void actionPerformed(ActionEvent e) {
        Editor.this.editMode = EditMode.ADD_NEIGHBORS;
        rebuildSidePanel();
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

    for (int i = 0; i < territoryListModel.size(); i++) { // Color in all submitted territories.
      Territory territory = territoryListModel.get(i);
      for (Point point : territory.getSeedPoints()) {
        ImageUtil.bucketFill(base, point, Constants.SUBMITTED_COLOR);
      }
    }

    for (Territory territory : finishedTerritories) {
      for (Point point : territory.getSeedPoints()) {
        ImageUtil.bucketFill(base, point, Constants.FINISHED_COLOR);
      }
    }

    for (Territory territory : neighbors) {
      for (Point point : territory.getSeedPoints()) {
        ImageUtil.bucketFill(base, point, Constants.NEIGHBOR_SELECT_COLOR);
      }
    }

    if (neighborModeSelected != null) {
      for (Point point : neighborModeSelected.getSeedPoints()) {
        ImageUtil.bucketFill(base, point, Constants.SELECT_COLOR);
      }
    }

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
    buttonArea.setLayout(new GridLayout(2, 1));

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
        addTerritoryButton.setPreferredSize(new Dimension(10, 25));
        buttonArea.add(deleteTerritoryButton);
        deleteTerritoryButton.addMouseListener(removeTerritoryButtonListener());
      }
      case ADD_NEIGHBORS -> {
        sidePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2), "Submit Neighbors", TitledBorder.CENTER, TitledBorder.CENTER));

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
      @Override
      public void mouseClicked(MouseEvent e) {
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
            if (neighborModeSelected != null) {
              // Only need to allow clicking FINISHED_COLOR if I add one-way neighbors. Atm it's sometimes necessary.
              if (ImageUtil.getPixelColor(base, cursor).equals(Constants.SUBMITTED_COLOR) || ImageUtil.getPixelColor(base, cursor).equals(Constants.FINISHED_COLOR)) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                  Territory t = getTerritory(cursor);
                  if (t != null && !t.equals(neighborModeSelected)) {
                    neighbors.add(t);
                  }
                }
              } else if (ImageUtil.getPixelColor(base, cursor).equals(Constants.NEIGHBOR_SELECT_COLOR)) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                  Territory t = getTerritory(cursor);
                  neighbors.remove(t);
                }
              } else if (ImageUtil.getPixelColor(base, cursor).equals(Constants.SELECT_COLOR)) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                  neighborModeSelected = null;
                  neighbors = new HashSet<>();
                }
              }
            } else {
              if (ImageUtil.getPixelColor(base, cursor).equals(Constants.SUBMITTED_COLOR)) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                  neighborModeSelected = getTerritory(cursor);
                  neighbors.addAll(graph.getNeighbors(neighborModeSelected));
                }
              } else if (neighborModeSelected == null && neighbors.isEmpty() && ImageUtil.getPixelColor(base, cursor).equals(Constants.FINISHED_COLOR)) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                  neighborModeSelected = getTerritory(cursor);
                  if (neighborModeSelected != null) {
                    neighbors.addAll(graph.getNeighbors(neighborModeSelected));
                  }
                }
              }
            }
          }
          case NO_EDIT -> {
          }
        }
        rebuildMapPanel();
      }
    };
  }

  private MouseInputAdapter addTerritoryButtonListener() {
    return new MouseInputAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        JTextArea nameArea = new JTextArea();
        nameArea.setEditable(true);

        String name = JOptionPane.showInputDialog(nameArea, "Enter territory name:");
        // TODO: Ask for territory name, get set of seedpoints to instantiate with
        if (name == null || name.isEmpty()) {
          return;
        }
        if (activePoints.isEmpty()) {
          return;
        }
        territoryListModel.addElement(new Territory(name, new HashSet<>(activePoints)));
        activePoints.clear();
        rebuildMapPanel();
      }
    };
  }

  private MouseInputAdapter removeTerritoryButtonListener() {
    return new MouseInputAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        Territory selected = territoryJList.getSelectedValue();
        if (selected != null) {
          for (Point point : selected.getSeedPoints()) {
            ImageUtil.bucketFill(base, point, Constants.TERRITORY_COLOR);
          }
          territoryListModel.removeElement(selected);
          rebuildMapPanel();
        }
      }
    };
  }

  private MouseInputAdapter submitNeighborsButtonListener() {
    return new MouseInputAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (neighborModeSelected != null && neighbors.size() > 0) {
          Optional<Point> point = neighborModeSelected.getSeedPoints().stream().findFirst();
          if (point.isPresent()) {
            Territory done = getTerritory(point.get());
            Set<Territory> doneNeighbors = new HashSet<>(neighbors);

            if (graph.getNodes().contains(done) && finishedTerritories.contains(done)) {
              Set<Edge<Territory>> edgesToRemove = new HashSet<>();
              Set<Territory> currentNeighbors = graph.getNeighbors(done);
              currentNeighbors.removeAll(doneNeighbors);
              for (Territory oldNeighbor : currentNeighbors) {
                for (Edge<Territory> edge : graph.getEdges()) {
                  if ((edge.getSource().equals(done) && edge.getTarget().equals(oldNeighbor)) || (edge.getTarget().equals(done) && edge.getSource().equals(oldNeighbor))) {
                    edgesToRemove.add(edge);
                  }
                }
              }
              graph.removeEdges(edgesToRemove);
            }

            finishedTerritories.add(done);
            for (Territory t : doneNeighbors) {
              graph.putEdge(done, t);
            }
            neighborModeSelected = null;
            neighbors = new HashSet<>();
          }
        }
        rebuildMapPanel();
      }
    };
  }

  // Utility

  private Territory getTerritory(Point point) {
    Point rootPoint = ImageUtil.getRootPixel(base, point);
    for (int i = 0; i < territoryListModel.size(); i++) {
      Territory territory = territoryListModel.get(i);
      if (territory.getSeedPoints().contains(rootPoint)) {
        return territory;
      }
    }
    return null;
  }

}
