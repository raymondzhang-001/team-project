package view;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * contain map tile
 *
 * - 기본 위치: 토론토 근처
 * - 드래그로 지도 이동 가능
 * - setCenter(lat, lon)으로 중심 이동
 * - 지도를 클릭하면 그 위치에 핀(마커)이 추가됨
 */
public class MapPanel extends JPanel {

    private final JXMapViewer mapViewer;

    // 🔹 마커(핀)들을 저장하는 집합
    private final Set<Waypoint> waypoints = new HashSet<>();
    // 🔹 마커들을 그려주는 Painter
    private final WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<>();

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

        // 🔹 마커 페인터를 JXMapViewer에 등록
        waypointPainter.setWaypoints(waypoints);
        mapViewer.setOverlayPainter(waypointPainter);

        add(mapViewer, BorderLayout.CENTER);

        // mouse drag (기존 기능 그대로 유지)
        enableDragging();

        // 🔹 클릭하면 해당 위치에 마커 추가
        enableClickToAddMarker();
    }

    private void enableDragging() {
        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);
    }

    /**
     * 지도 클릭 시 그 위치에 핀(마커)을 추가하는 리스너 설정
     */
    private void enableClickToAddMarker() {
        mapViewer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                GeoPosition gp = mapViewer.convertPointToGeoPosition(e.getPoint());
                addMarker(gp);
            }
        });
    }

    /**
     * 외부(SearchView)에서 검색 결과 기준으로 중심을 옮길 때 사용하는 메서드
     */
    public void setCenter(double latitude, double longitude) {
        mapViewer.setAddressLocation(new GeoPosition(latitude, longitude));
        mapViewer.repaint();
    }

    public JXMapViewer getMapViewer() {
        return mapViewer;
    }

    /**
     * 내부적으로 지도 위에 마커(핀)를 추가하고 다시 그린다.
     */
    private void addMarker(GeoPosition position) {
        waypoints.add(new DefaultWaypoint(position));
        waypointPainter.setWaypoints(waypoints);
        mapViewer.repaint();
    }

    public static class HttpsOsmTileFactoryInfo extends OSMTileFactoryInfo {
        public HttpsOsmTileFactoryInfo() {
            super("OpenStreetMap HTTPS",
                    "https://tile.openstreetmap.org"
            );
        }
    }
}


