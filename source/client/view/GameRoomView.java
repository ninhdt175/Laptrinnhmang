/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package client.view;

import client.network.ClientSocketManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.util.HashMap;
import java.util.Map;

public class GameRoomView extends JFrame {
    private JLabel lblRoomInfo;
    private JLabel lblTimer;
    private JLabel lblWordHint;
    private JButton btnStartGame;
    private JButton btnLeaveRoom;

    private PaintCanvas paintCanvas;
    private ToolPanel toolPanel;

    private DefaultTableModel scoreboardModel;
    private JTable tblScoreboard;

    private JTextArea txtChatArea;
    private JTextField txtChatInput;
    private JButton btnSendChat;

    private String currentRoomId = "";
    private String currentHost = "";
    private boolean gameInProgress = false;
    private final Map<String, Integer> playerScores = new HashMap<>();

    public GameRoomView() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Phòng chơi - Vẽ Hình Đoán Chữ");
        setSize(1080, 680);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        // 1. Header Panel (NORTH)
        JPanel headerPanel = new JPanel(new BorderLayout(10, 5));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        lblRoomInfo = new JLabel("Phòng: ---");
        lblRoomInfo.setFont(new Font("Arial", Font.BOLD, 15));
        lblTimer = new JLabel("⏱ --s");
        lblTimer.setFont(new Font("Arial", Font.BOLD, 16));
        lblTimer.setForeground(new Color(192, 57, 43));
        leftHeader.add(lblRoomInfo);
        leftHeader.add(lblTimer);

        lblWordHint = new JLabel("Đang chờ chủ phòng bắt đầu...", SwingConstants.CENTER);
        lblWordHint.setFont(new Font("Arial", Font.BOLD, 20));
        lblWordHint.setForeground(new Color(41, 128, 185));

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnStartGame = new JButton("Bắt đầu");
        btnStartGame.setBackground(new Color(39, 174, 96));
        btnStartGame.setForeground(Color.WHITE);
        btnStartGame.setVisible(false);

        btnLeaveRoom = new JButton("Rời phòng");
        btnLeaveRoom.setBackground(new Color(231, 76, 60));
        btnLeaveRoom.setForeground(Color.WHITE);

        rightHeader.add(btnStartGame);
        rightHeader.add(btnLeaveRoom);

        headerPanel.add(leftHeader, BorderLayout.WEST);
        headerPanel.add(lblWordHint, BorderLayout.CENTER);
        headerPanel.add(rightHeader, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Left Panel: Scoreboard & Players (WEST)
        JPanel westPanel = new JPanel(new BorderLayout(5, 5));
        westPanel.setPreferredSize(new Dimension(210, 0));
        westPanel.setBorder(BorderFactory.createTitledBorder("Bảng điểm & Người chơi"));

        scoreboardModel = new DefaultTableModel(new String[]{"Người chơi", "Điểm"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblScoreboard = new JTable(scoreboardModel);
        tblScoreboard.setRowHeight(24);
        westPanel.add(new JScrollPane(tblScoreboard), BorderLayout.CENTER);
        add(westPanel, BorderLayout.WEST);

        // 3. Center Panel: ToolPanel + PaintCanvas (CENTER)
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        paintCanvas = new PaintCanvas();
        toolPanel = new ToolPanel(paintCanvas);
        toolPanel.setToolsEnabled(false);

        centerPanel.add(toolPanel, BorderLayout.NORTH);
        centerPanel.add(paintCanvas, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // 4. Right Panel: Chat & Guess Box (EAST)
        JPanel eastPanel = new JPanel(new BorderLayout(5, 5));
        eastPanel.setPreferredSize(new Dimension(280, 0));
        eastPanel.setBorder(BorderFactory.createTitledBorder("Khung Chat & Đoán chữ"));

        txtChatArea = new JTextArea();
        txtChatArea.setEditable(false);
        txtChatArea.setLineWrap(true);
        txtChatArea.setWrapStyleWord(true);
        txtChatArea.setFont(new Font("Arial", Font.PLAIN, 13));

        JPanel chatInputPanel = new JPanel(new BorderLayout(5, 0));
        txtChatInput = new JTextField();
        btnSendChat = new JButton("Gửi");
        chatInputPanel.add(txtChatInput, BorderLayout.CENTER);
        chatInputPanel.add(btnSendChat, BorderLayout.EAST);

        eastPanel.add(new JScrollPane(txtChatArea), BorderLayout.CENTER);
        eastPanel.add(chatInputPanel, BorderLayout.SOUTH);
        add(eastPanel, BorderLayout.EAST);
    }

    public void addStartGameListener(ActionListener listener) {
        btnStartGame.addActionListener(listener);
    }

    public void addLeaveRoomListener(ActionListener listener) {
        btnLeaveRoom.addActionListener(listener);
    }

    public void addSendChatListener(ActionListener listener) {
        btnSendChat.addActionListener(listener);
        txtChatInput.addActionListener(listener);
    }

    public void addWindowClosingListener(WindowListener listener) {
        this.addWindowListener(listener);
    }

    public void btnStartGameActionPerformed(ActionEvent evt, Runnable logic) {
        logic.run();
    }

    public void setRoomLabel(String roomId) {
        this.currentRoomId = roomId;
        String me = ClientSocketManager.getInstance().getCurrentUsername();
        setTitle("Phòng: " + roomId + (me != null ? " | Người chơi: " + me : ""));
        lblRoomInfo.setText("Phòng: " + roomId);
    }

    public String getChatMessage() {
        return txtChatInput.getText().trim();
    }

    public void clearChatInput() {
        txtChatInput.setText("");
    }

    public void appendChat(String sender, String message) {
        txtChatArea.append("[" + sender + "]: " + message + "\n");
        txtChatArea.setCaretPosition(txtChatArea.getDocument().getLength());
    }

    public void updateRoom(String roomId, String playersCsv, String hostUsername) {
        setRoomLabel(roomId);
        this.currentHost = hostUsername != null ? hostUsername.trim() : "";
        String me = ClientSocketManager.getInstance().getCurrentUsername();
        boolean isHost = me != null && me.equalsIgnoreCase(this.currentHost);
        btnStartGame.setVisible(isHost && !gameInProgress);

        scoreboardModel.setRowCount(0);
        if (playersCsv != null && !playersCsv.isBlank()) {
            for (String p : playersCsv.split(",")) {
                String name = p.trim();
                if (!name.isEmpty()) {
                    int score = playerScores.getOrDefault(name, 0);
                    String display = name.equalsIgnoreCase(this.currentHost) ? name + " (Chủ phòng)" : name;
                    scoreboardModel.addRow(new Object[]{display, score});
                }
            }
        }
    }

    public void handleRoomUpdate(String[] params) {
        if (params.length >= 3) {
            updateRoom(params[0], params[1], params[2]);
        }
    }

    public void showRoundStart(int round, String drawerUsername) {
        gameInProgress = true;
        btnStartGame.setVisible(false);
        paintCanvas.clearCanvas();
        String me = ClientSocketManager.getInstance().getCurrentUsername();
        boolean amIDrawer = me != null && me.equalsIgnoreCase(drawerUsername);
        paintCanvas.setDrawable(amIDrawer);
        toolPanel.setToolsEnabled(amIDrawer);
        lblWordHint.setText("Vòng " + round + " — Người vẽ: " + drawerUsername);
        appendChat("HỆ THỐNG", "Bắt đầu vòng " + round + "! Người vẽ: " + drawerUsername);
    }

    public void showSecretWord(String word, String category) {
        lblWordHint.setText("BẠN VẼ — Từ bí mật: " + word + " (Chủ đề: " + category + ")");
        paintCanvas.setDrawable(true);
        toolPanel.setToolsEnabled(true);
        paintCanvas.clearCanvas();
    }

    public void showWordHint(String pattern, String category) {
        lblWordHint.setText("Gợi ý: " + pattern + "  |  Chủ đề: " + category);
        paintCanvas.setDrawable(false);
        toolPanel.setToolsEnabled(false);
    }

    public void setTimer(int seconds) {
        lblTimer.setText("⏱ " + seconds + "s");
    }

    public void updateLeaderboard(String leaderboardCsv) {
        if (leaderboardCsv == null || leaderboardCsv.isBlank()) return;
        scoreboardModel.setRowCount(0);
        for (String item : leaderboardCsv.split(",")) {
            String[] kv = item.trim().split(":");
            if (kv.length >= 2) {
                String name = kv[0].trim();
                int score = 0;
                try {
                    score = Integer.parseInt(kv[1].trim());
                } catch (NumberFormatException ignored) {}
                playerScores.put(name, score);
                String display = name.equalsIgnoreCase(this.currentHost) ? name + " (Chủ phòng)" : name;
                scoreboardModel.addRow(new Object[]{display, score});
            }
        }
    }

    public void showRoundEnd(String secretWord) {
        paintCanvas.setDrawable(false);
        toolPanel.setToolsEnabled(false);
        lblWordHint.setText("Hết vòng! Từ khóa là: " + secretWord);
    }

    public void setGameOverState() {
        gameInProgress = false;
        paintCanvas.setDrawable(false);
        toolPanel.setToolsEnabled(false);
        lblTimer.setText("⏱ --s");
        lblWordHint.setText("Trận đấu kết thúc! Chờ chủ phòng bắt đầu ván mới...");
        String me = ClientSocketManager.getInstance().getCurrentUsername();
        boolean isHost = me != null && me.equalsIgnoreCase(this.currentHost);
        btnStartGame.setVisible(isHost);
    }

    public PaintCanvas getPaintCanvas() {
        return paintCanvas;
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameRoomView lb = new GameRoomView();
            lb.setVisible(true);
        });
    }
}