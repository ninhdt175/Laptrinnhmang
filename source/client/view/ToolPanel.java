/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package client.view;

import client.network.ClientSocketManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ToolPanel extends JPanel {
    private final PaintCanvas canvas;
    private JButton btnColor;
    private JSlider sliderSize;
    private JToggleButton btnEraser;
    private JButton btnClear;

    public ToolPanel(PaintCanvas canvas) {
        this.canvas = canvas;
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
        setBorder(BorderFactory.createEtchedBorder());
        initComponents();
    }

    private void initComponents() {
        btnColor = new JButton("Chọn màu");
        btnColor.setBackground(Color.BLACK);
        btnColor.setForeground(Color.WHITE);
        //btnColor.addActionListener(e -> btnColorActionPerformed(e));

        JLabel lblSize = new JLabel("Độ dày cọ:");
        sliderSize = new JSlider(2, 20, 4);
        sliderSize.setPreferredSize(new Dimension(110, 20));
        sliderSize.addChangeListener(e -> sliderSizeStateChanged(e));

        btnEraser = new JToggleButton("Tẩy");
        btnEraser.addActionListener(e -> btnEraserActionPerformed(e));

        btnClear = new JButton("Xóa");
        btnClear.addActionListener(e -> btnClearActionPerformed(e));

        add(btnColor);
        add(lblSize);
        add(sliderSize);
        add(btnEraser);
        add(btnClear);
    }


    public void sliderSizeStateChanged(javax.swing.event.ChangeEvent evt) {
        canvas.setStrokeSize(sliderSize.getValue());
    }

    public void btnEraserActionPerformed(ActionEvent evt) {
        boolean isEraserActive = btnEraser.isSelected();
        canvas.setEraser(isEraserActive);
    }

    public void btnClearActionPerformed(ActionEvent evt) {
        canvas.clearCanvas();
        //ClientSocketManager.getInstance().sendPacket("CLEAR_CANVAS");
    }

    public void setToolsEnabled(boolean enabled) {
        btnColor.setEnabled(enabled);
        sliderSize.setEnabled(enabled);
        btnEraser.setEnabled(enabled);
        btnClear.setEnabled(enabled);
    }
}
