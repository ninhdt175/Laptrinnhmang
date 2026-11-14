package client.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class LoginView extends JFrame {
    private final JTextField txtUsername = new JTextField();
    // private final JTextField txtHost = new JTextField("127.0.0.1");
    // private final JTextField txtPort = new JTextField("8888");
    
    private final JButton btnLogin = new JButton("Đăng nhập");

    public LoginView() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Đăng nhập");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 320);
        setResizable(false);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(245, 247, 250));

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        main.setBackground(new Color(245, 247, 250));

        JLabel title = new JLabel("VẼ HÌNH ĐOÁN CHỮ", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(41, 128, 185));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        main.add(title);
        main.add(Box.createRigidArea(new Dimension(0, 20)));

 
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        userPanel.setBackground(new Color(245, 247, 250));
        userPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblUser = new JLabel("Tên người chơi:");
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblUser.setPreferredSize(new Dimension(100, 25));
        
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsername.setPreferredSize(new Dimension(180, 30));
        txtUsername.setToolTipText("Nhập tên");

        userPanel.add(lblUser);
        userPanel.add(txtUsername);
        main.add(userPanel);

        /* 
        main.add(Box.createRigidArea(new Dimension(0, 10)));
        JPanel hostPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        hostPanel.setBackground(new Color(245, 247, 250));
        hostPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel lblHost = new JLabel("Địa chỉ Server:");
        lblHost.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblHost.setPreferredSize(new Dimension(100, 25));
        txtHost.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtHost.setPreferredSize(new Dimension(180, 30));
        hostPanel.add(lblHost);
        hostPanel.add(txtHost);
        main.add(hostPanel);

        main.add(Box.createRigidArea(new Dimension(0, 10)));
        
        JPanel portPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        portPanel.setBackground(new Color(245, 247, 250));
        portPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel lblPort = new JLabel("Cổng (Port):");
        lblPort.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblPort.setPreferredSize(new Dimension(100, 25));
        txtPort.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPort.setPreferredSize(new Dimension(180, 30));
        portPanel.add(lblPort);
        portPanel.add(txtPort);
        main.add(portPanel);
        */

        main.add(Box.createRigidArea(new Dimension(0, 25))); 

        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBackground(new Color(39, 174, 96));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnLogin.setMaximumSize(new Dimension(160, 38));
        btnLogin.setPreferredSize(new Dimension(160, 38));
        
        main.add(btnLogin);

        add(main);
        getRootPane().setDefaultButton(btnLogin);
    }

    public void addLoginListener(ActionListener listener) {
        btnLogin.addActionListener(listener);
    }

    public String getUsername() {
        return txtUsername.getText().trim();
    }

    public void setLoginButtonEnabled(boolean enabled) {
        btnLogin.setEnabled(enabled);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginView lv = new LoginView();
            lv.setVisible(true);
        });
    }
}
