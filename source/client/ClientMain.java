package client;

import client.controller.LoginController;
import client.view.LoginView;

import javax.swing.*;

/**
 * Điểm khởi chạy giao diện đồ họa Client - Trò chơi Vẽ hình đoán chữ.
 */
public class ClientMain {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            LoginView loginView = new LoginView();
            new LoginController(loginView);
            loginView.setVisible(true);
        });
    }
}

