package client.view;

import client.model.Stroke;
import client.network.ClientSocketManager;
import common.Protocol;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
public class PaintCanvas extends JPanel {
    private BufferedImage canvasImage;
    private Graphics2D g2d;
    private int lastX, lastY;
    private boolean mousePressed;
    private Color currentColor = Color.RED;
    private int strokeSize = 4;
    private boolean isEraser;
    private boolean isDrawable;

    public PaintCanvas() {
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!isDrawable || !SwingUtilities.isLeftMouseButton(e)) return;
                lastX = e.getX();
                lastY = e.getY();
                mousePressed = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mousePressed = false;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (!isDrawable || !mousePressed) return;
                int currentX = e.getX();
                int currentY = e.getY();
                Color drawColor = isEraser ? Color.WHITE : currentColor;
                Stroke stroke = new Stroke(lastX, lastY, currentX, currentY,
                        drawColor, strokeSize);
                drawStroke(stroke);
                ClientSocketManager.getInstance().sendPacket(
                        Protocol.DRAW_STROKE, stroke.toProtocolString());
                lastX = currentX;
                lastY = currentY;
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    private synchronized void initCanvasBuffer() {
        int width = Math.max(1, getWidth());
        int height = Math.max(1, getHeight());
        if (canvasImage == null || canvasImage.getWidth() != width
                || canvasImage.getHeight() != height) {
            BufferedImage newImage = new BufferedImage(
                    width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = newImage.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);

            if (canvasImage != null) {
                graphics.drawImage(canvasImage, 0, 0, null);
            }
            graphics.dispose();

            if (g2d != null) g2d.dispose();
            canvasImage = newImage;
            g2d = canvasImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
        }
    }

    public synchronized void drawStroke(Stroke stroke) {
        if (stroke == null) return;
        initCanvasBuffer();

        g2d.setColor(stroke.getColor());
        g2d.setStroke(new BasicStroke(stroke.getSize(),
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(stroke.getX1(), stroke.getY1(),
                stroke.getX2(), stroke.getY2());
        repaint();
    }

    public void drawRemoteStroke(String payload) {
        Stroke stroke = Stroke.fromProtocolString(payload);
        if (stroke != null) {
            drawStroke(stroke);
        }
    }

    public synchronized void clearCanvas() {
        initCanvasBuffer();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, canvasImage.getWidth(), canvasImage.getHeight());
        repaint();
    }

    @Override
    protected synchronized void paintComponent(Graphics g) {
        super.paintComponent(g);
        initCanvasBuffer();
        g.drawImage(canvasImage, 0, 0, null);
    }

    public void setCurrentColor(Color color) {
        if (color != null) {
            currentColor = color;
            isEraser = false;
        }
    }

    public void setStrokeSize(int size) {
        if (size >= 1 && size <= 50) strokeSize = size;
    }

    public void setEraser(boolean eraser) {
        isEraser = eraser;
    }
    public void setDrawable(boolean drawable) {
        isDrawable = drawable;
        if (!drawable) mousePressed = false;
    }
}
