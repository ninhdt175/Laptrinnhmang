package client.controller;

import client.network.*;
import client.view.*;
import common.Protocol;

import javax.swing.SwingUtilities;
public class GameRoomController implements ServerListener {
    private final GameRoomView view;
    private final ClientSocketManager network;
    private final String roomId;

    public GameRoomController(GameRoomView view, String roomId) {
        this.view = view;
        this.roomId = roomId;
        this.network = ClientSocketManager.getInstance();

        view.addStartGameListener(e -> startGame());
        view.addLeaveRoomListener(e -> leaveRoom());
        view.addSendChatListener(e -> sendChat());
        view.addWindowClosingListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                leaveRoom();
            }
        });
        network.addListener(this);
        view.setRoomLabel(roomId);
    }

    private void startGame() {
        if (!network.sendPacket(Protocol.START_GAME)) {
            view.showError("Không gửi được yêu cầu bắt đầu trận đấu.");
        }
    }

    private void sendChat() {
        String message = view.getChatMessage();
        if (message.isEmpty()) return;
        if (message.contains("|")) {
            view.showError("Tin nhắn không được chứa ký tự '|'.");
            return;
        }
        if (network.sendPacket(Protocol.CHAT_GUESS, message)) {
            view.clearChatInput();
        } else {
            view.showError("Không gửi được tin nhắn.");
        }
    }

    private void leaveRoom() {
        network.sendPacket(Protocol.LEAVE_ROOM);
        network.removeListener(this);
        view.dispose();

        LobbyView lobby = new LobbyView();
        new LobbyController(lobby);
        lobby.setVisible(true);
    }

    @Override
    public void onMessageReceived(String action, String[] params) {
        SwingUtilities.invokeLater(() -> handlePacket(action, params));
    }

    private void handlePacket(String action, String[] params) {
        switch (action) {
            case Protocol.ROOM_UPDATE:
                if (params.length >= 3) {
                    view.updateRoom(params[0], params[1], params[2]);
                }
                break;

            case Protocol.ROUND_START:
                if (params.length >= 2) {
                    int round = parseInt(params[0], 0);
                    view.showRoundStart(round, params[1]);
                }
                break;

            case Protocol.SECRET_WORD:
                if (params.length >= 2) {
                    view.showSecretWord(params[0], params[1]);
                }
                break;

            case Protocol.WORD_HINT:
                if (params.length >= 2) {
                    view.showWordHint(params[0], params[1]);
                }
                break;
            case Protocol.TIMER_TICK:
                if (params.length >= 1) {
                    view.setTimer(parseInt(params[0], 0));
                }
                break;

            case Protocol.SYNC_STROKE:
                if (params.length >= 1) {
                    view.getPaintCanvas().drawRemoteStroke(params[0]);
                }
                break;

            case Protocol.CLEAR_CANVAS:
                view.getPaintCanvas().clearCanvas();
                break;

            case Protocol.CHAT_MESSAGE:
                if (params.length >= 2) {
                    view.appendChat(params[0], params[1]);
                }
                break;

            case Protocol.CORRECT_GUESS:
                if (params.length >= 2) {
                    view.appendChat("HỆ THỐNG",
                            params[0] + " đoán đúng (+" + params[1] + " điểm).");
                }
                break;

            case Protocol.ROUND_END:
                if (params.length >= 2) {
                    view.updateLeaderboard(params[1]);
                    view.showRoundEnd(params[0]);
                    view.appendChat("HỆ THỐNG", "Kết thúc vòng. Từ khóa: " + params[0]);
                }
                break;

            case Protocol.GAME_OVER:
                if (params.length >= 2) {
                    view.updateLeaderboard(params[1]);
                    view.setGameOverState();
                    view.showMessage("Trận đấu kết thúc! Người chiến thắng: " + params[0]);
                }
                break;

            case "ERROR":
                view.showError(params.length > 0 ? params[0] : "Có lỗi từ Server.");
                break;

            default:
                // Bỏ qua packet không thuộc phạm vi giao diện
                break;
        }
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    @Override
    public void onDisconnected() {
        SwingUtilities.invokeLater(() -> {
            network.removeListener(this);
            if (view.isDisplayable()) {
                view.showError("Mất kết nối tới Server.");
                view.dispose();
            }
        });
    }
}
