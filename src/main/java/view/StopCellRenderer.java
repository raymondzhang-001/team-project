package view;

import javax.swing.*;
import java.awt.*;

/**
 * Simplified and cleaner version of your StopCellRenderer.
 * Functionally identical, but easier to read and maintain.
 * Responsibilities:
 *  - Render each stop with:
 *      * A numbered circular badge
 *      * Gradient route lines
 *      * Stop name text
 *  - Purely a View component (no controller or state mutation)
 */
public class StopCellRenderer implements ListCellRenderer<String> {

    private static final int CELL_HEIGHT = 52;
    private static final int BADGE_RADIUS = 14;
    private static final int BADGE_X = 12;
    private static final Color ROUTE_COLOR = new Color(0, 120, 255);
    private static final Color BADGE_COLOR = new Color(100, 100, 140);
    private static final Color SELECTED_BG = new Color(200, 220, 240);
    private static final Color NORMAL_BG = new Color(220, 235, 245);

    private final ListModel<String> model;
    private final Font font = new JLabel().getFont().deriveFont(14f);
    private final Stroke routeStroke = new BasicStroke(
            6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    public StopCellRenderer(ListModel<String> model) {
        this.model = model;
    }

    @Override
    public Component getListCellRendererComponent(
            JList<? extends String> list,
            String value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {

        return new StopCellPanel(
                value == null ? "" : value,
                index,
                isSelected,
                model.getSize(),
                font,
                routeStroke
        );
    }

    /**
     * Panel for rendering a single stop.
     * Split into helper methods for clarity.
     */
    private static class StopCellPanel extends JPanel {

        private final String name;
        private final int index;
        private final int total;
        private final Font cellFont;
        private final transient Stroke routeStroke;

        StopCellPanel(String name, int index, boolean isSelected,
                      int total, Font cellFont, Stroke routeStroke) {

            this.name = name;
            this.index = index;
            this.total = total;
            this.cellFont = cellFont;
            this.routeStroke = routeStroke;

            setOpaque(true);
            setPreferredSize(new Dimension(0, CELL_HEIGHT));
            setBackground(isSelected ? SELECTED_BG : NORMAL_BG);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int h = getHeight();
            int centerY = h / 2;
            int centerX = BADGE_X + BADGE_RADIUS;

            drawRouteSegments(g2, centerX, centerY);
            drawBadge(g2, centerY);
            drawName(g2, centerX, centerY);

            g2.dispose();
        }

        /* ------------------------ UI Drawing Helpers ------------------------ */

        private void drawRouteSegments(Graphics2D g2, int cx, int cy) {
            int segments = Math.max(0, total - 1);
            if (segments == 0) return;

            g2.setStroke(routeStroke);

            if (index > 0)
                drawGradientLine(g2, cx, 0, cx, cy - BADGE_RADIUS, index - 1, segments);

            if (index < total - 1)
                drawGradientLine(g2, cx, cy + BADGE_RADIUS, cx, getHeight(), index, segments);
        }

        private void drawGradientLine(Graphics2D g2,
                                      int x1, int y1, int x2, int y2,
                                      int segmentIndex, int totalSegments) {

            float t = (totalSegments == 1)
                    ? 0f
                    : (float) segmentIndex / (float) (totalSegments - 1);

            int alpha = interpolate(t);
            g2.setColor(new Color(ROUTE_COLOR.getRed(),
                    ROUTE_COLOR.getGreen(),
                    ROUTE_COLOR.getBlue(),
                    alpha));
            g2.drawLine(x1, y1, x2, y2);
        }

        private int interpolate(float t) {
            return Math.min(255, Math.max(0,
                    Math.round(255 + t * (76 - 255))));
        }

        private void drawBadge(Graphics2D g2, int cy) {
            int x = BADGE_X;
            int y = cy - BADGE_RADIUS;

            g2.setColor(BADGE_COLOR);
            g2.fillOval(x, y, BADGE_RADIUS * 2, BADGE_RADIUS * 2);

            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(x, y, BADGE_RADIUS * 2, BADGE_RADIUS * 2);

            g2.setFont(cellFont);
            String num = String.valueOf(index + 1);
            FontMetrics fm = g2.getFontMetrics();
            int tx = x + (BADGE_RADIUS * 2 - fm.stringWidth(num)) / 2;
            int ty = y + ((BADGE_RADIUS * 2 - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(num, tx, ty);
        }

        private void drawName(Graphics2D g2, int cx, int cy) {
            g2.setFont(cellFont);
            g2.setColor(Color.DARK_GRAY);
            int textX = cx + BADGE_RADIUS + 12;
            int textY = cy + g2.getFontMetrics().getAscent() / 2 - 2;
            g2.drawString(name, textX, textY);
        }
    }
}
