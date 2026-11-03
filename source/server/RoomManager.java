package server;

import common.Protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Quản lý danh sách các phòng chơi (Lobby) tập trung trên Server.
 */
public class RoomManager {

    private static final RoomManager instance = new RoomManager();

    public static RoomManager getInstance() {
        return instance;
    }

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    private RoomManager() {}

    /**
     * Tạo một phòng chơi mới với host là người yêu cầu
     */
    public synchronized Room createRoom(String roomId, ClientHandler host) {
        if (roomId == null || roomId.trim().isEmpty()) {
            return null;
        }
        String id = roomId.trim();
        if (rooms.containsKey(id)) {
            return null; // Phòng đã tồn tại
        }

        Room room = new Room(id, host);
        rooms.put(id, room);
        return room;
    }

    /**
     * Lấy phòng theo ID
     */
    public Room getRoom(String roomId) {
        if (roomId == null) return null;
        return rooms.get(roomId.trim());
    }

    /**
     * Xóa phòng khỏi hệ thống
     */
    public void removeRoom(String roomId) {
        if (roomId != null) {
            rooms.remove(roomId.trim());
        }
    }

    /**
     * Dọn dẹp các phòng trống không còn người chơi
     */
    public synchronized void cleanupEmptyRooms() {
        rooms.entrySet().removeIf(entry -> entry.getValue().getPlayerCount() == 0);
    }

    /**
     * Định dạng chuỗi danh sách phòng cho giao thức ROOM_LIST:
     * "room1:2/8,room2:5/8"
     */
    public String getRoomListString() {
        cleanupEmptyRooms();
        if (rooms.isEmpty()) {
            return "";
        }
        List<String> list = new ArrayList<>();
        for (Room room : rooms.values()) {
            list.add(room.getId() + ":" + room.getPlayerCount() + "/8 [" + room.getState().name() + "]");
        }
        return String.join(",", list);
    }
}
