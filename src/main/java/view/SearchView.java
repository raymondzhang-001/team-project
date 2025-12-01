package view;

import interface_adapter.generate_route.GenerateRouteController;
import interface_adapter.remove_marker.RemoveMarkerController;
import interface_adapter.search.SearchController;
import interface_adapter.search.SearchState;
import interface_adapter.search.SearchViewModel;
import interface_adapter.reorder.ReorderController;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.jxmapviewer.viewer.GeoPosition;

public class SearchView extends JPanel implements ActionListener, PropertyChangeListener {

    private final String viewName;
    private final SearchViewModel searchViewModel;
    final JPanel searchLocationPanel = new JPanel(new BorderLayout());
    private final JTextField searchInputField = new JTextField(15);
    private final JButton search =  new JButton("Search");
    private final JButton routeButton = new JButton("Route");

    private transient SearchController searchController = null;
    private transient RemoveMarkerController removeMarkerController = null;
    private transient ReorderController reorderController = null;
    private transient GenerateRouteController generateRouteController = null;
    private final MapPanel mapPanel = new MapPanel();

    private final DefaultListModel<String> stopsListModel = new DefaultListModel<>();
    private final JList<String> stopsList = new JList<>(stopsListModel);

    // progress UI for rerouting
    private final JPanel progressPanelContainer; // hold in bottom-right
    private final JProgressBar rerouteProgressBar;
    private final JLabel rerouteLabel;
    private Timer progressTimer;
    private int fakeProgress = 0;

    // Zoom controls container
    private final JPanel zoomControlsContainer;

    /**
     * Construct the SearchView JPanel from its SearchViewModel
     */
    public SearchView(SearchViewModel searchViewModel) {
        this.viewName = searchViewModel.getViewName();
        this.searchViewModel = searchViewModel;
        searchViewModel.addPropertyChangeListener(this);

        search.addActionListener(
                evt -> {
                    if (evt.getSource().equals(search)) {
                        final SearchState currentState = searchViewModel.getState();
                        // 确保 Controller 存在再调用
                        if (searchController != null) {
                            searchController.execute(currentState.getLocationName());
                        }
                    }
                }
        );

        routeButton.addActionListener(evt -> triggerRouteComputation());

        searchInputField.getDocument().addDocumentListener(new DocumentListener() {
            private void documentListenerHelper() {
                final SearchState currentState = searchViewModel.getState();
                currentState.setLocationName(searchInputField.getText());
                searchViewModel.setState(currentState);
            }

            @Override
            public void insertUpdate(DocumentEvent e) { documentListenerHelper(); }
            @Override
            public void removeUpdate(DocumentEvent e) { documentListenerHelper(); }
            @Override
            public void changedUpdate(DocumentEvent e) { documentListenerHelper(); }
        });

        this.setLayout(new BorderLayout());

        // Zoom controls container setup
        zoomControlsContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        zoomControlsContainer.setOpaque(false);

        // Zoom buttons logic
        JButton leftZoomBtn = new JButton("+");
        leftZoomBtn.setToolTipText("Zoom in");
        leftZoomBtn.setPreferredSize(new Dimension(36, 24));
        leftZoomBtn.setFont(leftZoomBtn.getFont().deriveFont(Font.BOLD, 14f));
        leftZoomBtn.setMargin(new Insets(2, 4, 2, 4));
        leftZoomBtn.setHorizontalAlignment(SwingConstants.CENTER);
        leftZoomBtn.addActionListener(evt -> {
            try {
                JComponent viewer = mapPanel.getMapViewer();
                GeoPosition center = mapPanel.getMapViewer().convertPointToGeoPosition(new Point(viewer.getWidth()/2, viewer.getHeight()/2));
                int z = mapPanel.getMapViewer().getZoom();
                int newZ = Math.max(0, z - 1);
                mapPanel.getMapViewer().setZoom(newZ);
                mapPanel.getMapViewer().setAddressLocation(center);
            } catch (Exception ignored) {}
        });

        JButton rightZoomBtn = new JButton("-");
        rightZoomBtn.setToolTipText("Zoom out");
        rightZoomBtn.setPreferredSize(new Dimension(36, 24));
        rightZoomBtn.setFont(rightZoomBtn.getFont().deriveFont(Font.BOLD, 14f));
        rightZoomBtn.setMargin(new Insets(2, 4, 2, 4));
        rightZoomBtn.setHorizontalAlignment(SwingConstants.CENTER);
        rightZoomBtn.addActionListener(evt -> {
            try {
                JComponent viewer = mapPanel.getMapViewer();
                GeoPosition center = mapPanel.getMapViewer().convertPointToGeoPosition(new Point(viewer.getWidth()/2, viewer.getHeight()/2));
                int z = mapPanel.getMapViewer().getZoom();
                int newZ = Math.min(20, z + 1);
                mapPanel.getMapViewer().setZoom(newZ);
                mapPanel.getMapViewer().setAddressLocation(center);
            } catch (Exception ignored) {}
        });

        JPanel zoomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        zoomPanel.setOpaque(false);
        zoomPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        zoomControlsContainer.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

        zoomPanel.add(leftZoomBtn);
        zoomPanel.add(rightZoomBtn);
        zoomControlsContainer.add(zoomPanel);

        zoomControlsContainer.setPreferredSize(new Dimension(0, 44));
        zoomControlsContainer.setVisible(true);

        this.add(zoomControlsContainer, BorderLayout.NORTH);

        // Left panel (sidebar)
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(new Color(70,130,180));
        leftPanel.setLayout(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(350, 800));

        JPanel topSearch = new JPanel(new BorderLayout());
        topSearch.setOpaque(false);
        topSearch.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topSearch.add(searchInputField, BorderLayout.CENTER);
        JPanel btns = new JPanel(new GridLayout(1,2,5,5));
        btns.setOpaque(false);
        btns.add(search);
        btns.add(routeButton);
        topSearch.add(btns, BorderLayout.EAST);

        // Ensure Enter triggers Search
        searchInputField.addActionListener(evt -> search.doClick());

        leftPanel.add(topSearch, BorderLayout.NORTH);

        // Stops list setup
        stopsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stopsList.setBackground(new Color(220, 235, 245));
        // Custom cell renderer
        stopsList.setCellRenderer(new ListCellRenderer<String>() {
            private final Font font = new JLabel().getFont().deriveFont(14f);

            class CellPanel extends JPanel {
                private String name = "";
                private int index = 0;
                private boolean isSelected = false;

                public CellPanel() {
                    setOpaque(true);
                    setPreferredSize(new Dimension(0, 52));
                }

                public void setData(String name, int index, boolean isSelected) {
                    this.name = name;
                    this.index = index;
                    this.isSelected = isSelected;
                    setBackground(isSelected ? new Color(200, 220, 240) : new Color(220, 235, 245));
                }

                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int w = getWidth();
                    int h = getHeight();
                    int radius = 14;
                    int badgeX = 12;
                    int centerX = badgeX + radius;
                    int centerY = h / 2;

                    int n = stopsListModel.getSize();
                    int segments = Math.max(0, n - 1);
                    Color routeColor = new Color(0, 120, 255);

                    if (index > 0 && segments > 0) {
                        int segIndex = index - 1;
                        float t = (segments == 1) ? 0f : ((float) segIndex) / (float) (segments - 1);
                        int alphaStart = 255;
                        int alphaEnd = Math.max(1, (int) Math.round(255 * 0.30));
                        int alpha = (int) Math.round(alphaStart + t * (alphaEnd - alphaStart));
                        alpha = Math.max(0, Math.min(255, alpha));
                        g2.setColor(new Color(routeColor.getRed(), routeColor.getGreen(), routeColor.getBlue(), alpha));
                        g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2.drawLine(centerX, 0, centerX, centerY - radius);
                    }

                    if (index < n - 1 && segments > 0) {
                        int segIndex = index;
                        float t = (segments == 1) ? 0f : ((float) segIndex) / (float) (segments - 1);
                        int alphaStart = 255;
                        int alphaEnd = Math.max(1, (int) Math.round(255 * 0.30));
                        int alpha = (int) Math.round(alphaStart + t * (alphaEnd - alphaStart));
                        alpha = Math.max(0, Math.min(255, alpha));
                        g2.setColor(new Color(routeColor.getRed(), routeColor.getGreen(), routeColor.getBlue(), alpha));
                        g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2.drawLine(centerX, centerY + radius, centerX, h);
                    }

                    int bx = badgeX;
                    int by = centerY - radius;
                    g2.setColor(new Color(100, 100, 140));
                    g2.fillOval(bx, by, radius * 2, radius * 2);
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawOval(bx, by, radius * 2, radius * 2);

                    String num = String.valueOf(index + 1);
                    g2.setFont(font);
                    FontMetrics fm = g2.getFontMetrics();
                    int tx = bx + (radius * 2 - fm.stringWidth(num)) / 2;
                    int ty = by + ((radius * 2 - fm.getHeight()) / 2) + fm.getAscent();
                    g2.setColor(Color.WHITE);
                    g2.drawString(num, tx, ty);

                    g2.setColor(Color.DARK_GRAY);
                    int nameX = bx + radius * 2 + 12;
                    int nameY = centerY + fm.getAscent() / 2 - 2;
                    g2.drawString(name, nameX, nameY);

                    g2.dispose();
                }
            }

            private final CellPanel panel = new CellPanel();

            @Override
            public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
                panel.setData(value == null ? "" : value, index, isSelected);
                return panel;
            }
        });
        JScrollPane listScroll = new JScrollPane(stopsList);
        listScroll.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        leftPanel.add(listScroll, BorderLayout.CENTER);

        JPanel controls = new JPanel(new GridLayout(1,3,5,5));
        controls.setOpaque(false);
        JButton up = new JButton("Up");
        JButton down = new JButton("Down");
        JButton remove = new JButton("Remove");
        controls.add(up);
        controls.add(down);
        controls.add(remove);
        leftPanel.add(controls, BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(mapPanel, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        split.setDividerLocation(350);
        split.setResizeWeight(0);
        split.setOneTouchExpandable(true);
        this.add(split, BorderLayout.CENTER);

        // Bottom-right progress panel
        JPanel progressBox = new JPanel();
        progressBox.setLayout(new BoxLayout(progressBox, BoxLayout.Y_AXIS));
        progressBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        progressBox.setBackground(new Color(255, 255, 255, 230));

        rerouteLabel = new JLabel("");
        rerouteLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        rerouteLabel.setFont(rerouteLabel.getFont().deriveFont(Font.PLAIN, 12f));
        progressBox.add(rerouteLabel);
        progressBox.add(Box.createRigidArea(new Dimension(0,6)));

        rerouteProgressBar = new JProgressBar(0, 100);
        rerouteProgressBar.setValue(0);
        rerouteProgressBar.setPreferredSize(new Dimension(220, 14));
        rerouteProgressBar.setForeground(new Color(0, 120, 255));
        rerouteProgressBar.setBorderPainted(false);
        rerouteProgressBar.setStringPainted(false);
        progressBox.add(rerouteProgressBar);

        progressPanelContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 8));
        progressPanelContainer.setOpaque(false);
        progressPanelContainer.add(progressBox);
        progressPanelContainer.setPreferredSize(new Dimension(0, 72));
        progressPanelContainer.setVisible(true);
        rerouteLabel.setText("");
        rerouteProgressBar.setVisible(false);

        this.add(progressPanelContainer, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(() -> {
            Dimension size = rightPanel.getSize();
            if (size.width == 0 || size.height == 0) {
                size = new Dimension(Math.max(800 - 350, 400), 600);
            }
            mapPanel.setBounds(0, 0, size.width, size.height);
        });

        rightPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                Dimension size = rightPanel.getSize();
                mapPanel.setBounds(0, 0, size.width, size.height);
            }
        });

        // wire up actions
        up.addActionListener(e -> moveSelected(-1));
        down.addActionListener(e -> moveSelected(1));
        remove.addActionListener(e -> removeSelected());

        // Global Enter
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "globalEnter");
        this.getActionMap().put("globalEnter", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (searchInputField.isFocusOwner()) {
                    search.doClick();
                    return;
                }
                routeButton.doClick();
            }
        });

        // List-specific keys
        InputMap listIm = stopsList.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap listAm = stopsList.getActionMap();
        listIm.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "listUp");
        listIm.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "listDown");
        listIm.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "listRemove");
        listIm.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "listRemove");
        listAm.put("listUp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { moveSelected(-1); }
        });
        listAm.put("listDown", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { moveSelected(1); }
        });
        listAm.put("listRemove", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { removeSelected(); }
        });

        // Simplified map click listener: Uses coordinates directly, no DAO dependency
        mapPanel.setClickListener(gp -> {
            String name = String.format("%.5f, %.5f", gp.getLatitude(), gp.getLongitude());
            addStop(name, gp);
        });

        stopsList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int idx = stopsList.locationToIndex(evt.getPoint());
                    // Correct CA: Get data from ViewModel state instead of DAO
                    List<GeoPosition> stops = searchViewModel.getState().getStops();
                    if (idx >= 0 && idx < stops.size()) {
                        GeoPosition p = stops.get(idx);
                        mapPanel.setCenter(p.getLatitude(), p.getLongitude());
                    }
                }
            }
        });
    }

    private void showRerouteProgress() {
        SwingUtilities.invokeLater(() -> {
            fakeProgress = 0;
            rerouteProgressBar.setValue(0);
            rerouteProgressBar.setVisible(true);
            rerouteLabel.setText("Rerouting...");
            if (progressTimer != null && progressTimer.isRunning()) progressTimer.stop();
            progressTimer = new Timer(120, e -> {
                int add = 3 + (int) (Math.random() * 6);
                fakeProgress = Math.min(95, fakeProgress + add);
                rerouteProgressBar.setValue(fakeProgress);
            });
            progressTimer.start();
        });
    }

    private void hideRerouteProgress() {
        SwingUtilities.invokeLater(() -> {
            if (progressTimer != null) {
                progressTimer.stop();
            }
            rerouteProgressBar.setValue(100);
            Timer t = new Timer(300, e -> {
                rerouteProgressBar.setVisible(false);
                rerouteLabel.setText("");
                ((Timer) e.getSource()).stop();
            });
            t.setRepeats(false);
            t.start();
        });
    }

    private void triggerRouteComputation() {
        if (generateRouteController == null) {
            JOptionPane.showMessageDialog(this, "Routing backend not configured.");
            return;
        }

        showRerouteProgress();
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                generateRouteController.generate("walking", searchViewModel.getState().getStops());
                return null;
            }
        };
        worker.execute();
    }

    private void addStop(String name, GeoPosition gp) {
        SearchState current = new SearchState(searchViewModel.getState());
        List<String> names = new ArrayList<>(current.getStopNames());
        names.add(name);
        current.setStopNames(names);

        List<GeoPosition> updatedStops = new ArrayList<>(current.getStops());
        updatedStops.add(gp);
        current.setStops(updatedStops);
        current.setErrorMessage(null);
        searchViewModel.setState(current);
        searchViewModel.firePropertyChange("stops");
    }

    private void moveSelected(int delta) {
        int idx = stopsList.getSelectedIndex();
        if (idx == -1) return;
        int newIdx = idx + delta;
        if (reorderController != null) {
            reorderController.move(idx, newIdx, searchViewModel.getState().getStopNames(),
                    searchViewModel.getState().getStops());
        }
    }

    private void removeSelected() {
        int idx = stopsList.getSelectedIndex();
        if (removeMarkerController != null) {
            removeMarkerController.removeAt(idx, searchViewModel.getState().getStopNames(),
                    searchViewModel.getState().getStops());
        }
    }

    private void computeAndDisplayRouteIfAuto() {
        List<GeoPosition> stops = searchViewModel.getState().getStops();
        if (generateRouteController != null && stops.size() >= 2) {
            triggerRouteComputation();
        } else if (stops.size() < 2) {
            mapPanel.clearRoute();
        }
    }

    public void actionPerformed(ActionEvent evt) {
        System.out.println("Click " + evt.getActionCommand());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Object value = evt.getNewValue();
        if (value instanceof SearchState state) {
            handleSearchState(state, evt.getPropertyName());
        }
    }

    private void handleSearchState(SearchState state, String propertyName) {
        if ("stops".equals(propertyName)) {
            stopsListModel.clear();
            for (String name : state.getStopNames()) {
                stopsListModel.addElement(name);
            }
            if (!stopsListModel.isEmpty()) {
                stopsList.setSelectedIndex(Math.max(0, Math.min(stopsListModel.size() - 1, stopsList.getSelectedIndex())));
            }
            mapPanel.setStops(state.getStops());
            if (state.getStops().size() < 2) {
                mapPanel.clearRoute();
            }
            computeAndDisplayRouteIfAuto();
            return;
        }

        if ("route".equals(propertyName)) {
            mapPanel.setRouteSegments(state.getRouteSegments());
            hideRerouteProgress();
            return;
        }

        if ("error".equals(propertyName) && state.getErrorMessage() != null) {
            JOptionPane.showMessageDialog(this, state.getErrorMessage());
            hideRerouteProgress();
            return;
        }

        setFields(state);
        if (state.getSearchError() != null) {
            JLabel label = new JLabel(state.getSearchError());
            label.setOpaque(true);
            label.setBackground(new Color(255, 255, 150));
            Window window = SwingUtilities.getWindowAncestor(this);
            Popup popup = PopupFactory.getSharedInstance()
                    .getPopup(window, label, 700, 400);
            popup.show();
            // --- FIX START: 使用全限定名 java.awt.event.AWTEventListener 解决报错 ---
            Toolkit.getDefaultToolkit().addAWTEventListener(new java.awt.event.AWTEventListener() {
                @Override
                public void eventDispatched(AWTEvent event) {
                    if (event instanceof MouseEvent me && me.getID() == MouseEvent.MOUSE_PRESSED) {
                        popup.hide();
                        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
                    }
                }
            }, AWTEvent.MOUSE_EVENT_MASK);
            // --- FIX END ---
        } else {
            mapPanel.setCenter(state.getLatitude(), state.getLongitude());
            addStop(state.getLocationName(), new GeoPosition(state.getLatitude(), state.getLongitude()));
            computeAndDisplayRouteIfAuto();
        }
    }

    private void setFields(SearchState state) {
        searchInputField.setText(state.getLocationName());
    }

    public String getViewName() {
        return viewName;
    }

    public void setSearchController(SearchController searchController) {
        this.searchController = searchController;
    }

    public void setRemoveMarkerController(RemoveMarkerController removeMarkerController) {
        this.removeMarkerController = removeMarkerController;
    }

    public void setReorderController(ReorderController reorderController) {
        this.reorderController = reorderController;
    }

    public void setGenerateRouteController(GenerateRouteController generateRouteController) {
        this.generateRouteController = generateRouteController;
    }
}