# MODULE SERVER - TRÒ CHƠI "VẼ HÌNH ĐOÁN CHỮ" (PICTIONARY)

> 📘 Module phụ trách toàn bộ logic nghiệp vụ Máy chủ, quản lý kết nối TCP đa luồng, phòng chơi, đồng bộ bảng vẽ thời gian thực và điều phối vòng đời trò chơi.

---

## 🎯 MỤC TIÊU & CHỨC NĂNG

Server chịu trách nhiệm:
1. **Quản lý Kết nối Đa luồng (Multi-threading)**: Tiếp nhận đồng thời nhiều Client qua TCP Socket bằng `ExecutorService` (Cached ThreadPool).
2. **Quản lý Tài khoản Trực tuyến**: Kiểm tra trùng lặp tên người chơi trong thời gian thực.
3. **Quản lý Sảnh & Phòng chơi (Lobby & Rooms)**:
   - Tạo phòng, tham gia phòng, thoát phòng, tự động chuyển quyền chủ phòng khi host rời đi.
   - Giới hạn 2 - 8 người chơi / phòng. Tự động dọn dẹp phòng trống.
4. **Đồng bộ Bảng vẽ Thời gian thực (Real-time Drawing Sync)**:
   - Chuyển tiếp tọa độ nét vẽ (`Stroke`: x1, y1, x2, y2, color, size) từ người vẽ (Drawer) tới tất cả người đoán (Guessers).
   - Đồng bộ lệnh xóa bảng (`CLEAR_CANVAS`).
5. **Vòng đời Trò chơi (Game Loop State Machine)**:
   - Xoay vòng người vẽ công bằng qua từng round.
   - Kho từ vựng tiếng Việt phong phú phân chia theo chủ đề (Động vật, Đồ vật, Phương tiện, Ẩm thực).
   - Tạo gợi ý độ dài từ khóa (`_ _ _   _ _ _`).
   - Đếm ngược thời gian vòng chơi (60 giây) bằng `ScheduledExecutorService`.
   - Chấm điểm đoán đúng theo tốc độ phản xạ và thưởng điểm cho người vẽ.

---

## ⚙️ CÔNG NGHỆ SỬ DỤNG

| Thành phần | Công nghệ | Chi tiết |
|------------|-----------|----------|
| Ngôn ngữ | Java | JDK 17 / 21 |
| Giao thức mạng | TCP Socket | `java.net.ServerSocket`, `java.net.Socket` |
| Xử lý đa luồng | Concurrency | `ExecutorService`, `ScheduledExecutorService` |
| Cấu trúc dữ liệu | Thread-Safe Collections | `CopyOnWriteArrayList`, `ConcurrentHashMap` |
| Mã hóa ký tự | UTF-8 | Hỗ trợ tiếng Việt có dấu hoàn chỉnh |

---

## 📂 KIẾN TRÚC MÃ NGUỒN SERVER

```
source/
├── common/
│   ├── Protocol.java          # Danh mục hằng số giao thức mạng chuẩn hóa
│   ├── Player.java            # Mô hình dữ liệu Người chơi (tên, điểm, vai trò)
│   └── Stroke.java            # Mô hình tọa độ nét vẽ (x1, y1, x2, y2, color, size)
└── server/
    ├── Server.java            # Điểm khởi chạy ServerSocket (Port 8888)
    ├── ClientHandler.java     # Luồng xử lý giao tiếp riêng biệt cho từng Client
    ├── RoomManager.java       # Quản lý danh sách các phòng chơi (Lobby)
    ├── Room.java              # Logic phòng chơi, Game Loop, Timer & Chấm điểm
    ├── WordDictionary.java    # Kho từ điển tiếng Việt & thuật toán so khớp không dấu
    └── BackendQuickTest.java  # Bộ kiểm thử tích hợp tự động toàn diện Backend
```

---

## 🚀 HƯỚNG DẪN BIÊN DỊCH VÀ KHỞI CHẠY

### 1. Biên dịch toàn bộ dự án
Mở terminal/cmd tại thư mục `source/`:
```bash
javac -encoding UTF-8 common/*.java server/*.java client/*.java
```

### 2. Khởi chạy Server
```bash
# Chạy với cổng mặc định 8888
java -cp . server.Server

# Hoặc chỉ định cổng tùy ý
java -cp . server.Server 9999
```

### 3. Chạy kiểm thử tự động toàn diện (Integration Test)
```bash
java -cp . server.BackendQuickTest
```

---

## 🔗 QUY CHUẨN GIAO THỨC TRUYỀN THÔNG (TCP PROTOCOL)

Định dạng gói tin: `HÀNH_ĐỘNG|THAM_SỐ_1|THAM_SỐ_2|...` kết thúc bằng ký tự xuống dòng `\n`.

| Giao thức | Chiều | Mô tả cú pháp |
|-----------|-------|---------------|
| `LOGIN` | C -> S | `LOGIN\|username` |
| `LOGIN_SUCCESS` | S -> C | `LOGIN_SUCCESS\|username` |
| `LOGIN_FAILED` | S -> C | `LOGIN_FAILED\|lý_do` |
| `GET_ROOMS` | C -> S | `GET_ROOMS` |
| `ROOM_LIST` | S -> C | `ROOM_LIST\|room1:2/8,room2:5/8` |
| `CREATE_ROOM` | C -> S | `CREATE_ROOM\|roomId` |
| `JOIN_ROOM` | C -> S | `JOIN_ROOM\|roomId` |
| `LEAVE_ROOM` | C -> S | `LEAVE_ROOM` |
| `ROOM_UPDATE` | S -> C | `ROOM_UPDATE\|roomId\|player1,player2...\|hostUsername` |
| `START_GAME` | C -> S | `START_GAME` (chỉ Chủ phòng gửi) |
| `ROUND_START` | S -> C | `ROUND_START\|roundNumber\|drawerUsername` |
| `SECRET_WORD` | S -> C | `SECRET_WORD\|word\|category` (chỉ gửi Người vẽ) |
| `WORD_HINT` | S -> C | `WORD_HINT\|hintPattern\|category` (gửi Người đoán) |
| `DRAW_STROKE` | C -> S | `DRAW_STROKE\|x1,y1,x2,y2,colorHex,size` (Người vẽ gửi) |
| `SYNC_STROKE` | S -> C | `SYNC_STROKE\|x1,y1,x2,y2,colorHex,size` (gửi người đoán) |
| `CLEAR_CANVAS` | C <-> S | `CLEAR_CANVAS` (Xóa toàn bộ bảng vẽ) |
| `CHAT_GUESS` | C -> S | `CHAT_GUESS\|nội_dung_đoán_hoặc_chat` |
| `CHAT_MESSAGE` | S -> C | `CHAT_MESSAGE\|người_gửi\|nội_dung` |
| `CORRECT_GUESS` | S -> C | `CORRECT_GUESS\|người_đoán\|điểm_cộng` |
| `TIMER_TICK` | S -> C | `TIMER_TICK\|số_giây_còn_lại` |
| `ROUND_END` | S -> C | `ROUND_END\|từ_khóa_bí_mật\|user1:điểm,user2:điểm...` |
| `GAME_OVER` | S -> C | `GAME_OVER\|người_thắng\|bảng_điểm_chung_cuộc` |
