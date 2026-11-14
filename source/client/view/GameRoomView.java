package client.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class GameRoomView extends JFrame {

    private JLabel lblRoom, lblTimer, lblWordHint;
    private JButton btnStartGame, btnLeave;
    private final ChatBoxPanel chatPanel = new ChatBoxPanel();
    private final JProgressBar timerBar = new JProgressBar(0, 60);
    private final PaintCanvas paintCanvas = new PaintCanvas();
    private final ToolPanel toolPanel = new ToolPanel(paintCanvas);
    private final ScoreboardPanel  scorePanel = new ScoreboardPanel();

    public GameRoomView() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Phòng chơi");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1120, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        lblRoom = new JLabel("Phòng", SwingConstants.LEFT);
        lblWordHint = new JLabel("Đang chờ chủ phòng bắt đầu...", SwingConstants.CENTER);
        lblTimer = new JLabel("60", SwingConstants.CENTER);
        btnStartGame = new JButton("Bắt đầu");
        btnLeave = new JButton("Rời phòng");

        JPanel header = new JPanel(new BorderLayout(8, 4));
        header.setBorder(BorderFactory.createEmptyBorder(8, 10, 4, 10));
        header.setBackground(new Color(23, 37, 84));
        lblRoom.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblRoom.setForeground(Color.WHITE);
        lblWordHint.setFont(new Font("Segoe UI", Font.BOLD, 19));
        lblWordHint.setForeground(Color.WHITE);
        lblTimer.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTimer.setForeground(Color.WHITE);
        lblTimer.setPreferredSize(new Dimension(60, 35));

        timerBar.setPreferredSize(new Dimension(140, 14));
        timerBar.setBorderPainted(false);
        timerBar.setBackground(Color.WHITE);
        timerBar.setForeground(new Color(34, 197, 94));

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerRight.setBackground(new Color(23, 37, 84));
        btnStartGame.setBackground(new Color(39, 174, 96));
        btnStartGame.setForeground(Color.WHITE);
        btnStartGame.setVisible(false);
        btnLeave.setBackground(new Color(71, 85, 105));
        btnLeave.setForeground(Color.WHITE);
        headerRight.add(timerBar);
        headerRight.add(lblTimer);
        headerRight.add(btnStartGame);
        headerRight.add(btnLeave);

        header.add(lblRoom, BorderLayout.WEST);
        header.add(lblWordHint, BorderLayout.CENTER);
        header.add(headerRight, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(5, 5));
        center.add(toolPanel, BorderLayout.NORTH);
        paintCanvas.setPreferredSize(new Dimension(700, 500));
        center.add(paintCanvas, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        add(scorePanel, BorderLayout.WEST);

        chatPanel.setPreferredSize(new Dimension(260, 260));
        add(chatPanel, BorderLayout.EAST);

        paintCanvas.setDrawable(false);
        toolPanel.setToolsEnabled(false);
    }

    public void addStartGameListener(ActionListener listener) {
        btnStartGame.addActionListener(listener);
    }

    public void addLeaveRoomListener(ActionListener listener) {
        btnLeave.addActionListener(listener);
    }

    public void addSendChatListener(ActionListener listener) {
        chatPanel.addSendListener(listener);
    }

    public PaintCanvas getPaintCanvas() {
        return paintCanvas;
    }

    public void setStartButtonVisible(boolean visible) {
        btnStartGame.setVisible(visible);
    }

    public void setRoomLabel(String roomId) {
        lblRoom.setText("Mã phòng: " + roomId);
    }

    public void updateRoom(String roomId, String players, String hostName) {
        setRoomLabel(roomId);
        btnStartGame.setVisible(hostName.equalsIgnoreCase(
                client.network.ClientSocketManager.getInstance().getCurrentUsername()));
    }

    public void setTimer(int seconds) {
        int remaining = Math.max(0, seconds);
        lblTimer.setText(remaining + "s");
        timerBar.setValue(Math.min(60, remaining));
        if (remaining > 30) {
            timerBar.setForeground(new Color(39, 174, 96));
        } else if (remaining > 10) {
            timerBar.setForeground(new Color(243, 156, 18));
        } else {
            timerBar.setForeground(new Color(231, 76, 60));
        }
    }

    public void updateScoreboard(String scoreboard) {
       scorePanel.updateScoreboard(scoreboard);
    }

    public void appendChat(String sender, String message, Color c) {
        chatPanel.appendMessage(sender, message, Color.BLACK);
    }

    public String getChatMessage() {
        return chatPanel.getInputText().trim();
    }

    public void clearChatInput() {
        chatPanel.clearInput();
    }

    public void appendCorrect(String sender, String message) {
        chatPanel.appendCorrectMessage(sender, message);
    }

    public void appendNormal(String sender, String message) {
        chatPanel.appendNormalMessage(sender, message);
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public void addWindowClosingListener(java.awt.event.WindowListener listener) {
        addWindowListener(listener);
    }

    public void setGameState(boolean b) {
        paintCanvas.setDrawable(b);
        toolPanel.setToolsEnabled(b);
    }

    public void showSecretWord(String word, String category) {
        lblWordHint.setText("Từ khóa: " + word + " (" + category + ")");
        setGameState(true);
    }

    public void showWordHint(String pattern, String category) {
        lblWordHint.setText("Gợi ý: " + pattern + " | Chủ đề: " + category);
        setGameState(false);
    }

    public void showRoundStart(int round, String drawer) {
        lblWordHint.setText("Vòng " + round + " – Người vẽ: " + drawer);
        setGameState(false);
        setTimer(60);
    }

    public void showRoundEnd(String answer) {
        lblWordHint.setText("Kết thúc vòng – Đáp án: " + answer);
        setGameState(false);
    }

    public void setGameOverState() {
        paintCanvas.setDrawable(false);
        setGameState(false);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameRoomView g = new GameRoomView();
            g.setVisible(true);
        });
    }
}
