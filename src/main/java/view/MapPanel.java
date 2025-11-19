package view;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import org.jxmapviewer.painter.CompoundPainter;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Arrays;

/**
 * contain map tile

 */
public class MapPanel extends JPanel {

    private final JXMapViewer mapViewer;

    // maintain list of marker positions in insertion order (so we can pick last two)
    private final List<GeoPosition> markerPositions = new ArrayList<>();

    private final Set<Waypoint> waypoints = new HashSet<>();

    private final WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<>();

    private RoutePainter routePainter; // initialize in constructor and allow reassignment
    private final CompoundPainter<JXMapViewer> compoundPainter = new CompoundPainter<>();

    public MapPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(800, 600));
        System.setProperty("http.agent", "MyMapApp/1.0 (contact@example.com)");

        HttpsOsmTileFactoryInfo info = new HttpsOsmTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);

        mapViewer = new JXMapViewer();
        mapViewer.setTileFactory(tileFactory);
        mapViewer.setAddressLocation(new GeoPosition(43.6532, -79.3832));
        mapViewer.setZoom(5);

        waypointPainter.setWaypoints(waypoints);
        // initialize route painter here
        routePainter = new RoutePainter(null);
        compoundPainter.setPainters(Arrays.asList(routePainter, waypointPainter));
        mapViewer.setOverlayPainter(compoundPainter);

        add(mapViewer, BorderLayout.CENTER);

        enableDragging();


        enableClickToAddMarker();
    }

    private void enableDragging() {
        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);
    }


    private void enableClickToAddMarker() {
        mapViewer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                GeoPosition gp = mapViewer.convertPointToGeoPosition(e.getPoint());
                addMarker(gp);
            }
        });
    }


    public void setCenter(double latitude, double longitude) {
        mapViewer.setAddressLocation(new GeoPosition(latitude, longitude));
        mapViewer.repaint();
    }

    public JXMapViewer getMapViewer() {
        return mapViewer;
    }


    private void addMarker(GeoPosition position) {
        // keep ordered list of marker positions
        markerPositions.add(position);
        // rebuild waypoint set from marker positions
        waypoints.clear();
        for (GeoPosition gp : markerPositions) {
            waypoints.add(new DefaultWaypoint(gp));
        }
        waypointPainter.setWaypoints(waypoints);
        mapViewer.repaint();
    }

    /**
     * Return a copy of last two marker positions (in order: earlier, later). If fewer than 2 markers, returns an empty list.
     */
    public List<GeoPosition> getLastTwoMarkerPositions() {
        int size = markerPositions.size();
        if (size < 2) return new ArrayList<>();
        GeoPosition a = markerPositions.get(size - 2);
        GeoPosition b = markerPositions.get(size - 1);
        return Arrays.asList(a, b);
    }

    public void setRoute(List<GeoPosition> route) {
        // create a fresh RoutePainter for the new route to avoid analyzer issues
        this.routePainter = new RoutePainter(route);
        compoundPainter.setPainters(Arrays.asList(routePainter, waypointPainter));
        mapViewer.repaint();
    }

    public void clearRoute() {
        setRoute(null);
    }

    public static class HttpsOsmTileFactoryInfo extends OSMTileFactoryInfo {
        public HttpsOsmTileFactoryInfo() {
            super("OpenStreetMap HTTPS",
                    "https://tile.openstreetmap.org"
            );
        }
    }
}
