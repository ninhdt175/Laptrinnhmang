package client.view;
import client.network.ClientSocketManager;
import common.Protocol;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ToolPanel extends JPanel {
    private final PaintCanvas canvas;
    private final JButton btnColor = new JButton("Màu bút");
    private final JSlider sliderSize = new JSlider(2, 20, 4);
    private final JToggleButton btnEraser = new JToggleButton("Tẩy");
    private final JButton btnClear = new JButton("Xóa bảng");

    public ToolPanel(PaintCanvas canvas) {
        this.canvas = canvas;
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
        setBorder(BorderFactory.createEtchedBorder());
        initComponents();
    }

    private void initComponents() {
        btnColor.setBackground(Color.BLACK);
        btnColor.setForeground(Color.WHITE);
        btnColor.addActionListener(e -> chooseColor());

        sliderSize.setPreferredSize(new Dimension(130, 24));
        sliderSize.setToolTipText("Độ dày nét vẽ");
        sliderSize.addChangeListener(e -> canvas.setStrokeSize(sliderSize.getValue()));

        btnEraser.addActionListener(this::toggleEraser);
        btnClear.addActionListener(this::clearCanvas);

        add(btnColor);
        add(new JLabel("Độ dày:"));
        add(sliderSize);
        add(btnEraser);
        add(btnClear);
    }

    private void chooseColor() {
        Color selected = JColorChooser.showDialog(this, "Chọn màu vẽ", btnColor.getBackground());
        if (selected != null) {
            canvas.setCurrentColor(selected);
            btnColor.setBackground(selected);
            btnColor.setForeground(isDark(selected) ? Color.WHITE : Color.BLACK);
            btnEraser.setSelected(false);
        }
    }

    private boolean isDark(Color color) {
        return (color.getRed() * 299 + color.getGreen() * 587
                + color.getBlue() * 114) / 1000 < 128;
    }

    private void toggleEraser(ActionEvent event) {
        canvas.setEraser(btnEraser.isSelected());
    }

    private void clearCanvas(ActionEvent event) {
        canvas.clearCanvas();
        ClientSocketManager.getInstance().sendPacket(Protocol.CLEAR_CANVAS);
    }

    public void setToolsEnabled(boolean enabled) {
        btnColor.setEnabled(enabled);
        sliderSize.setEnabled(enabled);
        btnEraser.setEnabled(enabled);
        btnClear.setEnabled(enabled);
    }
}
