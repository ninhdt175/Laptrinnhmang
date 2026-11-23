package client.view;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.*;

public class LobbyView extends JFrame {
    private DefaultTableModel tableModel;
    private JTable tblRoom;
    private final JLabel lblCurrentUser = new JLabel("", SwingConstants.RIGHT);
    private final JButton btnRefresh = new JButton("Làm mới");
    private final JButton btnCreateRoom = new JButton("Tạo phòng");
    private final JButton btnJoinRoom = new JButton("Tham gia");

    public LobbyView() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Sảnh chờ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(650, 430);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new BorderLayout(10, 4));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 16, 6, 16));

        JLabel title = new JLabel("DANH SÁCH PHÒNG", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(41, 128, 185));

        lblCurrentUser.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCurrentUser.setForeground(new Color(30, 41, 59));
        updateCurrentUserLabel();

        JLabel dummyLeft = new JLabel("");
        dummyLeft.setPreferredSize(new Dimension(180, 20));
        lblCurrentUser.setPreferredSize(new Dimension(180, 20));

        topPanel.add(dummyLeft, BorderLayout.WEST);
        topPanel.add(title, BorderLayout.CENTER);
        topPanel.add(lblCurrentUser, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
            new String[]{"Mã phòng", "Số người", "Trạng thái"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblRoom = new JTable(tableModel);
        tblRoom.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 13)
        );
        tblRoom.getTableHeader().setBackground(
                new Color(241, 245, 249)
        );
        tblRoom.getTableHeader().setForeground(new Color(30, 41, 59));
        tblRoom.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblRoom.setRowHeight(25);
        tblRoom.getTableHeader().setReorderingAllowed(false);
        add(new JScrollPane(tblRoom), BorderLayout.CENTER);

        styleButton(btnRefresh,  new Color(100, 116, 139));
        styleButton(btnCreateRoom,  new Color(22, 163, 74));
        styleButton(btnJoinRoom, new Color(37, 99, 235));
        
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.add(btnRefresh);
        bottom.add(btnCreateRoom);
        bottom.add(btnJoinRoom);
        add(bottom, BorderLayout.SOUTH);
    }

    public void addRefreshListener(ActionListener listener) {
        btnRefresh.addActionListener(listener);
    }

    public void addCreateRoomListener(ActionListener listener) {
        btnCreateRoom.addActionListener(listener);
    }

    public void addJoinRoomListener(ActionListener listener) {
        btnJoinRoom.addActionListener(listener);
    }

    public String getSelectedRoomId() {
        int row = tblRoom.getSelectedRow();
        if (row < 0) return null;
        int modelRow = tblRoom.convertRowIndexToModel(row);
        return String.valueOf(tableModel.getValueAt(modelRow, 0));
    }

    public void setRooms(ArrayList<String[]> rooms) {
        tableModel.setRowCount(0);
        for (String[] room : rooms) {
            tableModel.addRow(room);
        }
    }

    public void setBusy(boolean busy) {
        btnRefresh.setEnabled(!busy);
        btnCreateRoom.setEnabled(!busy);
        btnJoinRoom.setEnabled(!busy);
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public void updateCurrentUserLabel() {
        String username = client.network.ClientSocketManager.getInstance().getCurrentUsername();
        setPlayerName(username);
    }

    public void setPlayerName(String username) {
        if (username != null && !username.isEmpty()) {
            setTitle("Sảnh chờ - Người chơi: " + username);
            lblCurrentUser.setText("Người chơi: " + username);
        } else {
            setTitle("Sảnh chờ");
            lblCurrentUser.setText("Người chơi: Khách");
        }
    }
    private void styleButton(JButton button, Color color) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        button.setPreferredSize(new Dimension(125, 40));
    }
    public static void main(String[] args){
        SwingUtilities.invokeLater(()->{
            LobbyView lv = new LobbyView();
            lv.setVisible(true);
        });
    }
}
