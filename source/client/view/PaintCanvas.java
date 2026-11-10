/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package client.view;
import client.model.Stroke;
import client.network.ClientSocketManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
public class PaintCanvas extends JPanel {
    private BufferedImage canvasImage;
    private Graphics2D g2d;
    private int lastX,lastY;
    private Color currentColor = Color.BLACK;
    private int strokeSize = 4;
    private boolean isEraser = false, isDrawable = false;
    
    public PaintCanvas(){
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(Color.GRAY,2,true));
        MouseAdapter mouseAdapter = new MouseAdapter(){
            @Override 
            public void mousePressed(MouseEvent e){
                if(!isDrawable){
                    return;
                }
                lastX = e.getX();
                lastY = e.getY();
            }
            @Override
            public void mouseDragged(MouseEvent e){
                if(!isDrawable){
                    return;
                }
                int currentX = e.getX();
                int currentY = e.getY();
                Color drawColor = isEraser ? Color.WHITE : currentColor;
                Stroke s = new Stroke(lastX,lastY, currentX, currentY, drawColor,strokeSize);
                drawStroke(s);
                ClientSocketManager.getInstance().sendPacket("DRAW_STROKE",s.toProtocolString());
                lastX = currentX;
                lastY = currentY;
            }
        };
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }
    
    private void initCanvasBuffer(){
        if(canvasImage == null || 
                canvasImage.getWidth()!= getWidth()|| canvasImage.getHeight()!= getHeight()){
            int w = Math.max(1,getWidth());
            int h = Math.max(1,getHeight());
            BufferedImage newImage = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = newImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRect(0,0,w,h);
            if(canvasImage != null){
                g.drawImage(canvasImage,0,0,null);
            }
            g.dispose();
            canvasImage = newImage;
            g2d = canvasImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
    }
    public synchronized void drawStroke(Stroke stroke){
        if(stroke == null){
            return;
        }
        initCanvasBuffer();
        g2d.setColor(stroke.getColor());
        g2d.setStroke(new BasicStroke(stroke.getSize(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(stroke.getX1(), stroke.getY1(), stroke.getX2(), stroke.getY2());
        repaint();
    }
    protected void painComponent(Graphics g){
        super.paintComponent(g);
        initCanvasBuffer();
        g.drawImage(canvasImage,0,0,null);
    }
    public synchronized void drawRemoteStroke(String payload){
        Stroke stroke = Stroke.fromProtocolString(payload);
        drawStroke(stroke);
    }
    public synchronized void clearCanvas(){
        initCanvasBuffer();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0,0,getWidth(),getHeight());
        repaint();
        
    }
    public void setCurrentColor(Color color) { 
        this.currentColor = color; 
        this.isEraser = false; 
    }

    public void setStrokeSize(int size) { 
        this.strokeSize = size; 
    }

    public void setEraser(boolean eraser) { 
        this.isEraser = eraser; 
    }

    public void setDrawable(boolean drawable) { 
        this.isDrawable = drawable; 
    }
    
}
