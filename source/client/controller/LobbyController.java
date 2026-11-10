package client.controller;

import client.network.*;
import client.view.*;
import common.Protocol;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class LobbyController implements ServerListener {
    private final LobbyView view;
    private final ClientSocketManager network;

    public LobbyController(LobbyView view) {
        this.view = view;
        this.network = ClientSocketManager.getInstance();

        view.addRefreshListener(e -> refreshRooms());
        view.addCreateRoomListener(e -> createRoom());
        view.addJoinRoomListener(e -> joinSelectedRoom());
        network.addListener(this);

        refreshRooms();
    }

    private void refreshRooms() {
        if (!network.sendPacket(Protocol.GET_ROOMS)) {
            view.showError("Không thể yêu cầu danh sách phòng. Kết nối Server đã đóng.");
        }
    }

    private void createRoom() {
        String roomId = JOptionPane.showInputDialog(view, "Nhập mã phòng (không chứa ký tự |):",
                "Tạo phòng", JOptionPane.QUESTION_MESSAGE);
        if (roomId == null) return;
        roomId = roomId.trim();
        if (roomId.isEmpty() || roomId.length() > 20 || roomId.contains("|")
                || roomId.contains(",") || roomId.contains(":")) {
            view.showError("Mã phòng không hợp lệ. Hãy nhập từ 1 đến 20 ký tự, không chứa | , :.");
            return;
        }
        view.setBusy(true);
        if (!network.sendPacket(Protocol.CREATE_ROOM, roomId)) {
            view.setBusy(false);
            view.showError("Không gửi được yêu cầu tạo phòng.");
        }
    }

    private void joinSelectedRoom() {
        String roomId = view.getSelectedRoomId();
        if (roomId == null) {
            view.showMessage("Vui lòng chọn một phòng trước khi tham gia.");
            return;
        }
        view.setBusy(true);
        if (!network.sendPacket(Protocol.JOIN_ROOM, roomId)) {
            view.setBusy(false);
            view.showError("Không gửi được yêu cầu tham gia phòng.");
        }
    }

    private List<String[]> parseRoomList(String raw) {
        List<String[]> result = new ArrayList<>();
        if (raw == null || raw.isBlank()) return result;

        // Backend định dạng: roomId:2/8 [WAITING],roomId2:3/8 [PLAYING]
        for (String item : raw.split(",")) {
            String entry = item.trim();
            int colon = entry.lastIndexOf(':');
            int slash = entry.indexOf('/', colon + 1);
            int bracket = entry.indexOf('[', slash + 1);
            int closeBracket = entry.lastIndexOf(']');
            if (colon <= 0 || slash < 0 || bracket < 0 || closeBracket <= bracket) {
                continue;
            }
            String roomId = entry.substring(0, colon).trim();
            String players = entry.substring(colon + 1, slash).trim()
                    + "/" + entry.substring(slash + 1, bracket).trim();
            String state = entry.substring(bracket + 1, closeBracket).trim();
            result.add(new String[]{roomId, players, state});
        }
        return result;
    }

    @Override
    public void onMessageReceived(String action, String[] params) {
        SwingUtilities.invokeLater(() -> {
            if (Protocol.ROOM_LIST.equals(action)) {
                view.setRooms((ArrayList<String[]>) parseRoomList(params.length > 0 ? params[0] : ""));
                view.setBusy(false);
            } else if (Protocol.ROOM_UPDATE.equals(action)) {
                if (params.length >= 3) {
                    network.removeListener(this);
                    view.dispose();

                    GameRoomView gameView = new GameRoomView();
                    new GameRoomController(gameView, params[0]);
                    gameView.updateRoom(params[0], params[1], params[2]);
                    gameView.setVisible(true);
                }
            } else if ("ERROR".equals(action)) {
                view.setBusy(false);
                view.showError(params.length > 0 ? params[0] : "Thao tác không thành công.");
                refreshRooms();
            }
        });
    }

    @Override
    public void onDisconnected() {
        SwingUtilities.invokeLater(() -> {
            if (view.isDisplayable()) {
                view.setBusy(false);
                view.showError("Mất kết nối tới Server.");
            }
        });
    }
}
