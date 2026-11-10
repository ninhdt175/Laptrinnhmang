
package client.view;

import client.controller.LoginController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class LoginView extends JFrame {
    private JTextField txtUsername;
    private JTextField txtHost;
    private JTextField txtPort;
    private JButton btnLogin;

    public LoginView() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Đăng nhập - Vẽ Hình Đoán Chữ");
        setSize(400, 240);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        mainPanel.add(new JLabel("Tên người chơi:"));
        txtUsername = new JTextField("Player" + (int) (Math.random() * 900 + 100));
        mainPanel.add(txtUsername);

        mainPanel.add(new JLabel("Server IP:"));
        txtHost = new JTextField("127.0.0.1");
        mainPanel.add(txtHost);

        mainPanel.add(new JLabel("Cổng (Port):"));
        txtPort = new JTextField("8888");
        mainPanel.add(txtPort);

        btnLogin = new JButton("Đăng nhập");
        btnLogin.setBackground(new Color(46, 204, 113));
        btnLogin.setForeground(Color.WHITE);

        mainPanel.add(new JLabel());
        mainPanel.add(btnLogin);
        add(mainPanel);
        getRootPane().setDefaultButton(btnLogin);
    }

    public void addLoginListener(ActionListener listener) {
        btnLogin.addActionListener(listener);
    }

    public String getUsername() {
        return txtUsername.getText().trim();
    }

    public String getHost() {
        return txtHost.getText().trim();
    }

    public int getPort() throws NumberFormatException {
        return Integer.parseInt(txtPort.getText().trim());
    }

    public void setLoginButtonEnabled(boolean enabled) {
        btnLogin.setEnabled(enabled);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginView view = new LoginView();
            new LoginController(view);
            view.setVisible(true);
        });
    }
}

