package server;

import common.Player;
import common.Protocol;
import common.Stroke;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

/**
 * Xử lý kết nối TCP riêng biệt cho từng Client (chạy trên một luồng riêng).
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private Player player;
    private Room currentRoom;
    private volatile boolean isRunning = true;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public Player getPlayer() {
        return player;
    }

    public Room getRoom() {
        return currentRoom;
    }

    public void setRoom(Room room) {
        this.currentRoom = room;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

            String line;
            while (isRunning && (line = in.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    handleMessage(line);
                }
            }
        } catch (SocketException e) {
            // Client ngắt kết nối đột ngột
            System.out.println("[INFO] Client đóng kết nối: " + (player != null ? player.getUsername() : socket.getRemoteSocketAddress()));
        } catch (IOException e) {
            System.err.println("[ERROR] Lỗi I/O ClientHandler: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    /**
     * Phân tích và điều phối xử lý gói tin giao thức
     */
    private void handleMessage(String rawMessage) {
        String[] parts = rawMessage.split(Protocol.SPLIT_REGEX, -1);
        if (parts.length == 0) return;

        String action = parts[0].trim().toUpperCase();

        switch (action) {
            case Protocol.LOGIN:
                handleLogin(parts);
                break;

            case Protocol.GET_ROOMS:
                handleGetRooms();
                break;

            case Protocol.CREATE_ROOM:
                handleCreateRoom(parts);
                break;

            case Protocol.JOIN_ROOM:
                handleJoinRoom(parts);
                break;

            case Protocol.LEAVE_ROOM:
                handleLeaveRoom();
                break;

            case Protocol.START_GAME:
                handleStartGame();
                break;

            case Protocol.DRAW_STROKE:
                handleDrawStroke(parts);
                break;

            case Protocol.CLEAR_CANVAS:
                handleClearCanvas();
                break;

            case Protocol.CHAT_GUESS:
                handleChatGuess(parts);
                break;

            default:
                System.out.println("[WARN] Giao thức không xác định: " + rawMessage);
                send("ERROR|Giao thức không hợp lệ: " + action);
                break;
        }
    }

    private void handleLogin(String[] parts) {
        if (parts.length < 2 || parts[1].trim().isEmpty()) {
            send(Protocol.LOGIN_FAILED + "|Tên người chơi không được để trống!");
            return;
        }

        String username = parts[1].trim();
        if (username.length() < 2 || username.length() > 20) {
            send(Protocol.LOGIN_FAILED + "|Tên người chơi phải từ 2 đến 20 ký tự!");
            return;
        }

        if (Server.isUsernameTaken(username)) {
            send(Protocol.LOGIN_FAILED + "|Tên '" + username + "' đã có người sử dụng!");
            return;
        }

        // Đăng ký username thành công
        this.player = new Player(username);
        Server.registerUsername(username);

        send(Protocol.LOGIN_SUCCESS + "|" + username);
        System.out.println("[LOGIN] Người chơi đăng nhập thành công: " + username);
    }

    private void handleGetRooms() {
        String roomList = RoomManager.getInstance().getRoomListString();
        send(Protocol.ROOM_LIST + "|" + roomList);
    }

    private void handleCreateRoom(String[] parts) {
        if (player == null) {
            send("ERROR|Bạn cần đăng nhập trước khi tạo phòng!");
            return;
        }
        if (parts.length < 2 || parts[1].trim().isEmpty()) {
            send("ERROR|Mã phòng không được để trống!");
            return;
        }

        // Thoát phòng cũ nếu đang ở trong phòng
        handleLeaveRoom();

        String roomId = parts[1].trim();
        Room room = RoomManager.getInstance().createRoom(roomId, this);
        if (room == null) {
            send("ERROR|Phòng '" + roomId + "' đã tồn tại hoặc mã phòng không hợp lệ!");
            return;
        }

        System.out.println("[ROOM] " + player.getUsername() + " đã tạo phòng: " + roomId);
        room.broadcastRoomUpdate();
    }

    private void handleJoinRoom(String[] parts) {
        if (player == null) {
            send("ERROR|Bạn cần đăng nhập trước khi tham gia phòng!");
            return;
        }
        if (parts.length < 2 || parts[1].trim().isEmpty()) {
            send("ERROR|Mã phòng không được để trống!");
            return;
        }

        // Thoát phòng cũ nếu đang ở trong phòng
        handleLeaveRoom();

        String roomId = parts[1].trim();
        Room room = RoomManager.getInstance().getRoom(roomId);
        if (room == null) {
            send("ERROR|Phòng '" + roomId + "' không tồn tại!");
            return;
        }

        if (room.isFull()) {
            send("ERROR|Phòng '" + roomId + "' đã đầy (tối đa 8 người)!");
            return;
        }

        if (room.addClient(this)) {
            System.out.println("[ROOM] " + player.getUsername() + " đã vào phòng: " + roomId);
        } else {
            send("ERROR|Không thể tham gia phòng '" + roomId + "'!");
        }
    }

    private void handleLeaveRoom() {
        if (currentRoom != null) {
            String roomId = currentRoom.getId();
            currentRoom.removeClient(this);
            System.out.println("[ROOM] " + (player != null ? player.getUsername() : "Client") + " đã rời phòng: " + roomId);
            RoomManager.getInstance().cleanupEmptyRooms();
        }
    }

    private void handleStartGame() {
        if (currentRoom == null) {
            send("ERROR|Bạn chưa tham gia phòng nào!");
            return;
        }
        currentRoom.startGame(this);
    }

    private void handleDrawStroke(String[] parts) {
        if (currentRoom == null) return;
        if (parts.length < 2) return;

        Stroke stroke = Stroke.fromProtocolString(parts[1]);
        if (stroke != null) {
            currentRoom.handleDrawStroke(this, stroke);
        }
    }

    private void handleClearCanvas() {
        if (currentRoom == null) return;
        currentRoom.handleClearCanvas(this);
    }

    private void handleChatGuess(String[] parts) {
        if (currentRoom == null) {
            send("ERROR|Bạn chưa ở trong phòng nào để chat!");
            return;
        }
        if (parts.length < 2) return;
        currentRoom.handleChatMessage(this, parts[1]);
    }

    /**
     * Gửi tin nhắn qua Socket (luồng an toàn)
     */
    public synchronized void send(String message) {
        if (!isRunning || out == null) return;
        try {
            out.write(message);
            out.newLine();
            out.flush();
        } catch (IOException e) {
            System.err.println("[WARN] Gửi tin thất bại tới " + (player != null ? player.getUsername() : socket.getRemoteSocketAddress()));
            disconnect();
        }
    }

    /**
     * Đóng kết nối và dọn dẹp tài nguyên
     */
    public synchronized void disconnect() {
        isRunning = false;

        // Dọn dẹp phòng chơi
        if (currentRoom != null) {
            currentRoom.removeClient(this);
            currentRoom = null;
            RoomManager.getInstance().cleanupEmptyRooms();
        }

        // Giải phóng tên người chơi
        if (player != null) {
            Server.unregisterUsername(player.getUsername());
            System.out.println("[LOGOUT] Người chơi đã thoát: " + player.getUsername());
            player = null;
        }

        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }
}
