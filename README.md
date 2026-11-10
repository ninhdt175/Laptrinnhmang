# BÀI TẬP LỚN LẬP TRÌNH MẠNG — TRÒ CHƠI "VẼ HÌNH ĐOÁN CHỮ" (PICTIONARY)

**Giảng viên phụ trách:** TS. Đặng Ngọc Hùng  
**Khoa:** Công nghệ thông tin 1 — Học viện Công nghệ Bưu chính Viễn thông  
**Chủ đề:** Xây dựng trò chơi nhiều người chơi qua mạng TCP Socket (Pictionary / Vẽ hình đoán chữ thời gian thực)

---

## 🎯 GIỚI THIỆU & MỤC TIÊU DỰ ÁN

Dự án triển khai một trò chơi trực tuyến nhiều người chơi (Multiplayer Online Game) thời gian thực theo mô hình Client - Server trên nền tảng giao thức truyền thông **TCP Socket**.

### Các tính năng cốt lõi đã hoàn thiện:
1. **Quản lý Kết nối Đa luồng (Multi-threading Concurrency)**:
   - Máy chủ tiếp nhận đồng thời nhiều Client qua TCP Socket bằng `ExecutorService` (Cached ThreadPool), không gây nghẽn luồng.
   - Quản lý phiên làm việc, tự động dọn dẹp tài nguyên và ngắt kết nối an toàn khi Client thoát.
2. **Quản lý Tài khoản & Định danh trực tuyến**:
   - Xác thực người chơi, chống trùng lặp tên trong thời gian thực (`ConcurrentHashMap.newKeySet()`).
3. **Quản lý Sảnh & Phòng chơi (Lobby & Room Management)**:
   - Lấy danh sách phòng, tạo phòng, tham gia và rời phòng.
   - Giới hạn 2 - 8 người chơi / phòng.
   - Tự động chuyển quyền Chủ phòng (Host) khi chủ phòng cũ rời đi.
   - Tự động dọn dẹp phòng trống khi không còn thành viên.
4. **Đồng bộ Bảng vẽ Thời gian thực (Real-time Drawing Synchronization)**:
   - Chuyển tiếp tọa độ nét vẽ (`Stroke`: x1, y1, x2, y2, color, size) từ Người vẽ (Drawer) tới tất cả Người đoán (Guessers).
   - Đồng bộ thao tác xóa bảng (`CLEAR_CANVAS`).
   - Lưu trữ bộ nhớ đệm nét vẽ hiện tại để người chơi vào giữa chừng vẫn xem được toàn bộ hình đã vẽ.
5. **Vòng đời Trò chơi (Game Loop State Machine)**:
   - Quản lý 4 trạng thái phòng: `WAITING`, `PLAYING`, `ROUND_END`, `GAME_OVER`.
   - Tự động xoay vòng Người vẽ công bằng qua từng hiệp đấu.
   - Kho từ điển tiếng Việt phong phú phân loại theo chủ đề (*Động vật, Đồ vật, Phương tiện, Ẩm thực*).
   - Tự động tạo gợi ý số lượng ký tự (`_ _ _   _ _ _`) cho người đoán và gửi từ khóa bí mật cho người vẽ.
   - Bộ đếm thời gian mỗi hiệp (60 giây) bằng `ScheduledExecutorService` (`TIMER_TICK`).
   - Tự động kết thúc vòng sớm khi tất cả người đoán đều đã đoán đúng.
   - Chấm điểm động: đoán càng nhanh càng được nhiều điểm; thưởng điểm cho Người vẽ khi có người đoán trúng.
   - Thuật toán so khớp linh hoạt: kiểm tra đoán trúng cả khi gõ tiếng Việt có dấu lẫn **không dấu** (khử dấu NFD).
   - Chống gian lận: Người vẽ bị cấm tiết lộ từ khóa trong ô chat.

---

## ⚙️ CÔNG NGHỆ SỬ DỤNG

| Thành phần | Công nghệ | Chi tiết |
|------------|-----------|----------|
| Ngôn ngữ | Java | JDK 17 / 21 |
| Giao thức mạng | TCP Socket | `java.net.ServerSocket`, `java.net.Socket` |
| Xử lý đa luồng | Concurrency | `ExecutorService`, `ScheduledExecutorService` |
| Bộ sưu tập thread-safe | Java Collections | `CopyOnWriteArrayList`, `ConcurrentHashMap` |
| Mã hóa dữ liệu | UTF-8 | Hỗ trợ tiếng Việt có dấu đầy đủ |
| Giao diện người dùng | Java Swing / AWT | `JFrame`, `JPanel`, `Graphics2D` (đang hoàn thiện) |
| Công cụ kiểm thử | Tự động hóa & CLI | `BackendQuickTest` (Integration test), `TestConsoleClient` (CLI test) |

---

## 📂 CẤU TRÚC THƯ MỤC DỰ ÁN

```
assignment-network-project/
├── INSTRUCTION.md               # Quy định & tiêu chí chấm điểm (của giảng viên)
├── README.md                    # Tài liệu tổng quan dự án & hướng dẫn chạy (file này)
├── statics/                     # Thư mục chứa hình ảnh, sơ đồ kiến trúc
│   ├── logo.png
│   └── diagram.png
└── source/                      # Mã nguồn dự án
    ├── .gitignore
    ├── common/                  # Các lớp dùng chung giữa Client và Server
    │   ├── Protocol.java        # Danh mục hằng số giao thức mạng chuẩn hóa
    │   ├── Player.java          # Mô hình dữ liệu Người chơi (tên, điểm, vai trò)
    │   └── Stroke.java          # Mô hình tọa độ nét vẽ (x1, y1, x2, y2, color, size)
    ├── server/                  # Module phía Server (Backend)
    │   ├── Server.java          # Điểm khởi chạy ServerSocket (Port mặc định 8888)
    │   ├── ClientHandler.java   # Luồng xử lý giao tiếp riêng biệt cho từng Client
    │   ├── RoomManager.java     # Quản lý danh sách phòng chơi (Lobby)
    │   ├── Room.java            # Logic phòng chơi, Game Loop, Timer & Chấm điểm
    │   ├── WordDictionary.java  # Kho từ điển tiếng Việt & thuật toán so khớp không dấu
    │   └── BackendQuickTest.java# Bộ kiểm thử tích hợp tự động toàn diện Backend
    └── client/                  # Module phía Client
        ├── README.md            # Tài liệu kỹ thuật phía Client
        └── TestConsoleClient.java # Client dòng lệnh phục vụ kiểm thử độc lập
```

---

## 🔗 QUY CHUẨN GIAO THỨC TRUYỀN THÔNG (TCP PROTOCOL)

Giao thức được thiết kế theo dạng chuỗi văn bản UTF-8 kết thúc bởi ký tự xuống dòng `\n`.  
Cú pháp chuẩn: `HÀNH_ĐỘNG|THAM_SỐ_1|THAM_SỐ_2|...`

| Giao thức | Chiều | Cú pháp gói tin | Chức năng mô tả |
|-----------|-------|-----------------|-----------------|
| `LOGIN` | C $\rightarrow$ S | `LOGIN\|username` | Client yêu cầu đăng nhập với tên người chơi |
| `LOGIN_SUCCESS` | S $\rightarrow$ C | `LOGIN_SUCCESS\|username` | Server thông báo đăng nhập thành công |
| `LOGIN_FAILED` | S $\rightarrow$ C | `LOGIN_FAILED\|lý_do` | Server từ chối (tên trùng, ký tự không hợp lệ) |
| `GET_ROOMS` | C $\rightarrow$ S | `GET_ROOMS` | Yêu cầu danh sách các phòng đang mở |
| `ROOM_LIST` | S $\rightarrow$ C | `ROOM_LIST\|room1:2/8,room2:5/8` | Danh sách phòng kèm số lượng người hiện tại |
| `CREATE_ROOM` | C $\rightarrow$ S | `CREATE_ROOM\|roomId` | Tạo phòng chơi mới |
| `JOIN_ROOM` | C $\rightarrow$ S | `JOIN_ROOM\|roomId` | Tham gia vào một phòng chơi |
| `LEAVE_ROOM` | C $\rightarrow$ S | `LEAVE_ROOM` | Rời phòng hiện tại về lại sảnh |
| `ROOM_UPDATE` | S $\rightarrow$ C | `ROOM_UPDATE\|roomId\|p1,p2...\|host` | Cập nhật danh sách thành viên và tên Chủ phòng |
| `START_GAME` | C $\rightarrow$ S | `START_GAME` | Chủ phòng bấm bắt đầu ván đấu |
| `ROUND_START` | S $\rightarrow$ C | `ROUND_START\|round\|drawerUsername` | Thông báo mở hiệp mới và chỉ định Người vẽ |
| `SECRET_WORD` | S $\rightarrow$ C | `SECRET_WORD\|word\|category` | Gửi từ khóa bí mật (chỉ riêng Người vẽ nhận) |
| `WORD_HINT` | S $\rightarrow$ C | `WORD_HINT\|hintPattern\|category` | Gửi gợi ý độ dài `_ _ _` và chủ đề cho Người đoán |
| `DRAW_STROKE` | C $\rightarrow$ S | `DRAW_STROKE\|x1,y1,x2,y2,color,size` | Người vẽ gửi tọa độ nét vẽ lên Server |
| `SYNC_STROKE` | S $\rightarrow$ C | `SYNC_STROKE\|x1,y1,x2,y2,color,size` | Server chuyển tiếp nét vẽ cho các Người đoán |
| `CLEAR_CANVAS` | C $\leftrightarrow$ S | `CLEAR_CANVAS` | Lệnh xóa sạch bảng vẽ |
| `CHAT_GUESS` | C $\rightarrow$ S | `CHAT_GUESS\|nội_dung` | Gửi tin nhắn chat hoặc nhập từ đoán |
| `CHAT_MESSAGE` | S $\rightarrow$ C | `CHAT_MESSAGE\|người_gửi\|nội_dung` | Server broadcast tin nhắn phòng hoặc thông báo |
| `CORRECT_GUESS` | S $\rightarrow$ C | `CORRECT_GUESS\|user\|điểm_cộng` | Thông báo có người đoán chính xác từ khóa |
| `TIMER_TICK` | S $\rightarrow$ C | `TIMER_TICK\|giây_còn_lại` | Đồng bộ thời gian đếm ngược (mỗi giây một lần) |
| `ROUND_END` | S $\rightarrow$ C | `ROUND_END\|từ_khóa\|user1:điểm,...` | Kết thúc hiệp, công bố đáp án và bảng điểm |
| `GAME_OVER` | S $\rightarrow$ C | `GAME_OVER\|người_thắng\|bảng_điểm` | Kết thúc toàn bộ ván đấu, vinh danh người thắng |

---

## 🚀 HƯỚNG DẪN BIÊN DỊCH VÀ KHỞI CHẠY

### 1. Biên dịch toàn bộ mã nguồn
Mở Terminal hoặc Command Prompt tại thư mục `source/`:
```bash
cd source
javac -encoding UTF-8 common/*.java server/*.java client/*.java
```

### 2. Khởi chạy Máy chủ (Server)
```bash
# Chạy với cổng mặc định 8888
java -cp . server.Server

# Hoặc chỉ định cổng tùy chọn
java -cp . server.Server 9999
```

### 3. Chạy kiểm thử tự động toàn diện (Integration Test)
Chạy bộ test tự động hóa toàn bộ luồng mạng mà không cần thao tác tay:
```bash
java -cp . server.BackendQuickTest
```

### 4. Chạy Client dòng lệnh (TestConsoleClient)
Dùng để test thủ công các tính năng trực tiếp:
```bash
java -cp . client.TestConsoleClient
```
*Các lệnh hỗ trợ trong Console Client:*
- `/login <tên>` : Đăng nhập
- `/rooms` : Xem danh sách phòng
- `/create <mã_phòng>` : Tạo phòng mới
- `/join <mã_phòng>` : Vào phòng
- `/start` : Bắt đầu chơi (chủ phòng)
- `/draw <x1> <y1> <x2> <y2> <#colorHex> <size>` : Giả lập vẽ nét
- `/clear` : Xóa bảng vẽ
- `<từ cần đoán>` : Nhập trực tiếp nội dung để chat hoặc đoán từ
- `/leave` : Rời phòng
- `/quit` : Thoát

