package client.controller;

import client.network.*;
import client.view.*;
import common.Protocol;
import javax.swing.SwingUtilities;

public class LoginController implements ServerListener {
    private final LoginView view;
    private final ClientSocketManager network;

    public LoginController(LoginView view) {
        this.view = view;
        this.network = ClientSocketManager.getInstance();
        view.addLoginListener(e -> handleLogin());
        network.addListener(this);
    }

    private void handleLogin() {
        String username = view.getUsername().trim();

        final String host = view.getHost();
        final int port;
        try {
            port = Integer.parseInt(view.getPort());
            if (host.isBlank() || port < 1 || port > 65535) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            view.showError("Địa chỉ Server hoặc cổng kết nối không hợp lệ.");
            return;
        }
        
        view.setLoginButtonEnabled(false);
        
        if (!network.isConnected() && !network.connect(host, port)) {
            view.showError("Không thể kết nối Server tại " + host + ":" + port + ".");
            view.setLoginButtonEnabled(true);
            return;
        }
        
        if (!network.sendPacket(Protocol.LOGIN, username)) {
            view.showError("Không thể gửi yêu cầu đăng nhập. Vui lòng thử lại.");
            view.setLoginButtonEnabled(true);
        }
    }

    @Override
    public void onMessageReceived(String action, String[] params) {
        SwingUtilities.invokeLater(() -> {
            if (Protocol.LOGIN_SUCCESS.equals(action)) {
                String acceptedName = params.length > 0 ? params[0] : view.getUsername();
                network.setCurrentUsername(acceptedName);
                network.removeListener(this);
                view.dispose();

                LobbyView lobby = new LobbyView();
                new LobbyController(lobby);
                lobby.setVisible(true);
            } else if (Protocol.LOGIN_FAILED.equals(action)) {
                String reason = params.length > 0 ? params[0] : "Đăng nhập không thành công.";
                view.showError(reason);
                view.setLoginButtonEnabled(true);
            } else if ("ERROR".equals(action)) {
                view.showError(params.length > 0 ? params[0] : "Server trả về lỗi.");
                view.setLoginButtonEnabled(true);
            }
        });
    }

    @Override
    public void onDisconnected() {
        SwingUtilities.invokeLater(() -> {
            if (view.isDisplayable()) {
                view.setLoginButtonEnabled(true);
                view.showError("Mất kết nối tới Server. Vui lòng thử lại.");
            }
        });
    }
}
