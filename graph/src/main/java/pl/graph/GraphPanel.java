package pl.graph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GraphPanel extends JPanel {

    private Graph graph;
    private double scale = 1.0;
    private double offsetX = 0;
    private double offsetY = 0;
    private boolean showLabels = true;
    private boolean editMode = false;

    private int dragNodeIndex = -1;
    private Point lastMouse;

    public GraphPanel() {
        setBackground(Color.WHITE);
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMouse = e.getPoint();
                if (editMode && graph != null) {
                    int idx = findNodeNear(e.getX(), e.getY());
                    dragNodeIndex = idx;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragNodeIndex = -1;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragNodeIndex >= 0 && graph != null) {
                    Node n = graph.nodes[dragNodeIndex];
                    Point p = e.getPoint();
                    double gx = (p.x - offsetX) / scale;
                    double gy = (p.y - offsetY) / scale;
                    n.x = gx;
                    n.y = gy;
                    repaint();
                } else if (lastMouse != null) {
                    Point p = e.getPoint();
                    offsetX += p.x - lastMouse.x;
                    offsetY += p.y - lastMouse.y;
                    lastMouse = p;
                    repaint();
                }
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int notches = e.getWheelRotation();
                if (notches < 0) zoomIn(); else zoomOut();
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
        addMouseWheelListener(ma);
    }

    public void setGraph(Graph g) {
        this.graph = g;
        fitToWindow();
        repaint();
    }

    public Graph getGraph() {
        return this.graph;
    }

    public void setShowLabels(boolean show) {
        this.showLabels = show;
        repaint();
    }

    public void setEditMode(boolean edit) {
        this.editMode = edit;
    }

    public void zoomIn() {
        scale *= 1.2;
        repaint();
    }

    public void zoomOut() {
        scale /= 1.2;
        repaint();
    }

    public void fitToWindow() {
        if (graph == null || graph.nodesCount == 0) return;
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < graph.nodesCount; i++) {
            Node n = graph.nodes[i];
            minX = Math.min(minX, n.x);
            minY = Math.min(minY, n.y);
            maxX = Math.max(maxX, n.x);
            maxY = Math.max(maxY, n.y);
        }
        double w = Math.max(1, maxX - minX);
        double h = Math.max(1, maxY - minY);
        double panelW = getWidth() - 20.0;
        double panelH = getHeight() - 20.0;
        if (panelW <= 0 || panelH <= 0) return;
        scale = Math.min(panelW / w, panelH / h) * 0.9;
        offsetX = (getWidth() - (minX + maxX) * scale) / 2.0;
        offsetY = (getHeight() - (minY + maxY) * scale) / 2.0;
        repaint();
    }

    private int findNodeNear(int sx, int sy) {
        if (graph == null) return -1;
        double bestDist = 20.0;
        int best = -1;
        for (int i = 0; i < graph.nodesCount; i++) {
            Node n = graph.nodes[i];
            double x = n.x * scale + offsetX;
            double y = n.y * scale + offsetY;
            double d = Point.distance(sx, sy, x, y);
            if (d < bestDist) {
                bestDist = d;
                best = i;
            }
        }
        return best;
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (graph == null) {
            g.drawString("No graph loaded", 10, 20);
            g.dispose();
            return;
        }

        // draw edges
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < graph.edgesCount; i++) {
            Edge e = graph.edges[i];
            Node a = graph.nodes[e.firstNodeIndex];
            Node b = graph.nodes[e.secondNodeIndex];
            int x1 = (int) Math.round(a.x * scale + offsetX);
            int y1 = (int) Math.round(a.y * scale + offsetY);
            int x2 = (int) Math.round(b.x * scale + offsetX);
            int y2 = (int) Math.round(b.y * scale + offsetY);
            g.drawLine(x1, y1, x2, y2);
        }

        // draw nodes
        int r = Math.max(4, (int) Math.round(6 * scale / 50.0));
        g.setColor(Color.BLUE);
        for (int i = 0; i < graph.nodesCount; i++) {
            Node n = graph.nodes[i];
            int x = (int) Math.round(n.x * scale + offsetX);
            int y = (int) Math.round(n.y * scale + offsetY);
            g.fillOval(x - r, y - r, r * 2, r * 2);
            if (showLabels) {
                g.setColor(Color.BLACK);
                String label = Integer.toString(n.id);
                g.drawString(label, x + r + 2, y - r - 2);
                g.setColor(Color.BLUE);
            }
        }
        g.dispose();
    }
}
