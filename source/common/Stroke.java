package common;

import java.awt.Color;

/**
 * Đối tượng mô hình hóa một nét vẽ đoạn thẳng (từ x1, y1 đến x2, y2)
 * kèm theo màu sắc hex và độ dày nét cọ.
 */
public class Stroke {
    private int x1;
    private int y1;
    private int x2;
    private int y2;
    private String colorHex;
    private int size;

    public Stroke(int x1, int y1, int x2, int y2, String colorHex, int size) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.colorHex = colorHex != null ? colorHex : "#000000";
        this.size = Math.max(1, size);
    }

    public Stroke(int x1, int y1, int x2, int y2, Color color, int size) {
        this(x1, y1, x2, y2, colorToHex(color), size);
    }

    public static String colorToHex(Color c) {
        if (c == null) return "#000000";
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }

    public int getX1() { return x1; }
    public int getY1() { return y1; }
    public int getX2() { return x2; }
    public int getY2() { return y2; }
    public String getColorHex() { return colorHex; }
    public int getSize() { return size; }

    public Color getColor() {
        try {
            return Color.decode(colorHex);
        } catch (Exception e) {
            return Color.BLACK;
        }
    }

    /**
     * Chuyển thành chuỗi dữ liệu giao thức: x1,y1,x2,y2,colorHex,size
     */
    public String toProtocolString() {
        return x1 + "," + y1 + "," + x2 + "," + y2 + "," + colorHex + "," + size;
    }

    /**
     * Phân tích cú pháp chuỗi giao thức thành đối tượng Stroke
     */
    public static Stroke fromProtocolString(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        try {
            String[] parts = raw.split(",");
            if (parts.length >= 6) {
                int x1 = Integer.parseInt(parts[0].trim());
                int y1 = Integer.parseInt(parts[1].trim());
                int x2 = Integer.parseInt(parts[2].trim());
                int y2 = Integer.parseInt(parts[3].trim());
                String color = parts[4].trim();
                int size = Integer.parseInt(parts[5].trim());
                return new Stroke(x1, y1, x2, y2, color, size);
            }
        } catch (Exception e) {
            // Chuỗi dữ liệu lỗi
        }
        return null;
    }

    @Override
    public String toString() {
        return "Stroke[(" + x1 + "," + y1 + ") -> (" + x2 + "," + y2 + "), " + colorHex + ", size=" + size + "]";
    }
}
