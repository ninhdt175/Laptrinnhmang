# MODULE CLIENT - TRÒ CHƠI "VẼ HÌNH ĐOÁN CHỮ" (PICTIONARY)

> 📘 Module phụ trách Giao diện người dùng (Java Swing) và Bộ điều khiển kết nối mạng TCP Client.

---

## 🎯 MỤC TIÊU & CHỨC NĂNG

Client chịu trách nhiệm:
1. **Giao diện Người dùng (UI Swing)**:
   - Màn hình Đăng nhập (Login screen)
   - Màn hình Sảnh & Danh sách phòng (Lobby / Rooms list)
   - Màn hình Phòng chơi chính:
     - Bảng vẽ tương tác `DrawCanvas` (kế thừa `JPanel`, vẽ bằng `Graphics2D`).
     - Hộp công cụ màu sắc & kích thước cọ vẽ (Color picker & Brush size).
     - Khung Chat & Nhập từ đoán (Chat box & Input).
     - Đồng hồ đếm ngược (Countdown Timer) và Bảng xếp hạng điểm số (Scoreboard).
2. **Xử lý Mạng Nền (Network Worker Thread)**:
   - Kết nối tới Máy chủ qua TCP Socket (`java.net.Socket`).
   - Luồng nền đọc dữ liệu liên tục từ Server và bắn sự kiện cập nhật giao diện Swing bằng `SwingUtilities.invokeLater()`.

---

## ⚙️ CÔNG NGHỆ SỬ DỤNG

| Thành phần | Công nghệ | Chi tiết |
|------------|-----------|----------|
| Ngôn ngữ | Java | JDK 17 / 21 |
| Giao diện | Java Swing / AWT | `JFrame`, `JPanel`, `Graphics2D`, `JColorChooser` |
| Kết nối mạng | TCP Socket | `java.net.Socket`, `BufferedReader`, `BufferedWriter` |
| Mã hóa | UTF-8 | Tiếng Việt có dấu |

---

## 🧪 CÔNG CỤ TEST ĐỘC LẬP: `TestConsoleClient`

Để hỗ trợ phát triển và kiểm thử độc lập mà **không cần chờ đợi giao diện Swing**, nhóm đã xây dựng sẵn bộ Client dòng lệnh `TestConsoleClient.java`:

### Cách chạy Console Client:
```bash
# Mở terminal tại thư mục source/
java -cp . client.TestConsoleClient
```

### Các lệnh điều khiển trong Console Client:
- `/login <tên>` : Đăng nhập vào hệ thống
- `/rooms` : Xem danh sách các phòng đang mở
- `/create <mã_phòng>` : Tạo một phòng mới
- `/join <mã_phòng>` : Tham gia vào phòng chơi
- `/leave` : Rời phòng hiện tại
- `/start` : Bắt đầu ván đấu (dành cho Chủ phòng)
- `/draw x1 y1 x2 y2 #hex size` : Giả lập gửi nét vẽ (dành cho Người vẽ)
- `/clear` : Giả lập xóa bảng vẽ
- `<nội dung bất kỳ>` : Gửi tin nhắn chat hoặc dự đoán từ khóa
- `/quit` : Thoát ứng dụng
