package view;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Painter responsible for drawing a polyline route on a JXMapViewer.
 */
public class RoutePainter implements Painter<JXMapViewer> {

    private List<GeoPosition> route = new ArrayList<>();

    public RoutePainter(List<GeoPosition> route) {
        if (route != null) this.route = route;
    }

    public void setRoute(List<GeoPosition> route) {
        this.route = (route == null) ? new ArrayList<>() : route;
    }

    @Override
    public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
        if (route == null || route.size() < 2) return;

        // prepare graphics
        g = (Graphics2D) g.create();
        g.setColor(new Color(0, 120, 255, 180));
        g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GeneralPath path = new GeneralPath();
        boolean first = true;
        Rectangle viewport = map.getViewportBounds();

        for (GeoPosition gp : route) {
            Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
            double x = pt.getX() - viewport.getX();
            double y = pt.getY() - viewport.getY();

            if (first) {
                path.moveTo(x, y);
                first = false;
            } else {
                path.lineTo(x, y);
            }
        }

        g.draw(path);
        g.dispose();
    }
}

