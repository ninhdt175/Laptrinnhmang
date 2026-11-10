
package client.view;

import client.network.ClientSocketManager;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class LobbyView extends JFrame {
    private JTable tblRoom;
    private DefaultTableModel tableModel;
    private JButton btnRefresh;
    private JButton btnCreateRoom;
    private JButton btnJoinRoom;

    public LobbyView() {
        initComponents();
    }

    private void initComponents() {
        String user = ClientSocketManager.getInstance().getCurrentUsername();
        setTitle("Sảnh chờ" + (user != null ? " - Xin chào " + user : ""));
        setSize(650, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel lblTitle = new JLabel("DANH SÁCH PHÒNG CHƠI", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(lblTitle, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"Mã phòng", "Số người", "Trạng thái"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblRoom = new JTable(tableModel);
        tblRoom.setRowHeight(26);
        tblRoom.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tblRoom), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnRefresh = new JButton("Làm mới");
        btnCreateRoom = new JButton("Tạo phòng mới");
        btnJoinRoom = new JButton("Vào phòng");

        bottomPanel.add(btnRefresh);
        bottomPanel.add(btnCreateRoom);
        bottomPanel.add(btnJoinRoom);
        add(bottomPanel, BorderLayout.SOUTH);
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

    public int getSelectedRoomRow() {
        return tblRoom.getSelectedRow();
    }

    public Object getTableValueAt(int row, int col) {
        return tableModel.getValueAt(row, col);
    }

    public String getSelectedRoomId() {
        int row = tblRoom.getSelectedRow();
        if (row < 0) return null;
        Object val = tableModel.getValueAt(row, 0);
        return val != null ? val.toString().trim() : null;
    }

    public void setRooms(List<String[]> rooms) {
        tableModel.setRowCount(0);
        if (rooms != null) {
            for (String[] r : rooms) {
                tableModel.addRow(r);
            }
        }
    }

    public void setBusy(boolean busy) {
        btnRefresh.setEnabled(!busy);
        btnCreateRoom.setEnabled(!busy);
        btnJoinRoom.setEnabled(!busy);
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LobbyView lb = new LobbyView();
            lb.setVisible(true);
        });
    }
}
