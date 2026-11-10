/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package client.view;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameRoomView extends JFrame {
    private JLabel lblWordHint;
    private JButton btnStartGame;
    private PaintCanvas paintCanvas;
    private ToolPanel toolPanel;
    /*
    private ChatBoxPanel chatBoxPanel;
    private ScoreboardPanel scoreboardPanel;
    */
    public GameRoomView() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Phòng");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));


        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        lblWordHint = new JLabel("Đang chờ chủ phòng bắt đầu...", SwingConstants.CENTER);
        lblWordHint.setFont(new Font("Arial", Font.BOLD, 20));
        lblWordHint.setForeground(new Color(41, 128, 185));

        btnStartGame = new JButton("Bắt đầu");
        btnStartGame.setBackground(new Color(39, 174, 96));
        btnStartGame.setForeground(Color.WHITE);
        btnStartGame.setVisible(false); 

        headerPanel.add(lblWordHint, BorderLayout.CENTER);
        headerPanel.add(btnStartGame, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        paintCanvas = new PaintCanvas();
        toolPanel = new ToolPanel(paintCanvas);

        centerPanel.add(toolPanel, BorderLayout.NORTH);
        centerPanel.add(paintCanvas, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        /**
        scoreboardPanel = new ScoreboardPanel();
        add(scoreboardPanel, BorderLayout.WEST);

        chatBoxPanel = new ChatBoxPanel();
        add(chatBoxPanel, BorderLayout.EAST);
        */
    }

    
    public void addStartGameListener(ActionListener listener) {
        btnStartGame.addActionListener(listener);
    }
    
    public void btnStartGameActionPerformed(ActionEvent evt, Runnable logic) {
        logic.run();
    }

    public void handleRoomUpdate(String[] params) {
        if (params.length >= 2) {
            String playersStr = params[0];
            boolean isHost = Boolean.parseBoolean(params[1]);

            btnStartGame.setVisible(isHost);
            //scoreboardPanel.updateScores(playersStr);
        }
    }

    public void showSecretWord(String word, String category) {
        lblWordHint.setText("Từ bí mật: " + word + " (" + category + ")");
        paintCanvas.setDrawable(true);
        toolPanel.setToolsEnabled(true);
        paintCanvas.clearCanvas();
    }

    public void showWordHint(String pattern, String length) {
        lblWordHint.setText("Gợi ý: " + pattern + " (" + length + " ký tự)");
        paintCanvas.setDrawable(false);
        toolPanel.setToolsEnabled(false);
        paintCanvas.clearCanvas();
    }
    
      public PaintCanvas getPaintCanvas() {
        return paintCanvas;
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(()->{
            GameRoomView lb = new GameRoomView();
           lb.setVisible(true);
        });
    }
/*  
    public void updateScores(String scoreData) {
        scoreboardPanel.updateScores(scoreData);
    }

    public ChatBoxPanel getChatBoxPanel() {
        return chatBoxPanel;
    }
*/
}