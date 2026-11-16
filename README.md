# HƯỚNG DẪN CÀI ĐẶT VÀ CHẠY DỰ ÁN

## 1. Yêu cầu & Chuẩn bị Môi trường

1. **Cài đặt Java Development Kit (JDK):**
   - Yêu cầu **JDK 17** hoặc **JDK 21 LTS** trở lên (khuyến nghị [Eclipse Temurin](https://adoptium.net/) hoặc [Oracle JDK](https://www.oracle.com/java/technologies/downloads/)).
   - Mở Terminal / Command Prompt kiểm tra:
     ```bash
     java -version
     javac -version
     ```
2. **Tải mã nguồn dự án:**
   - Clone từ Git:
     ```bash
     git clone <URL_REPOSITORY>
     cd <TEN_THU_MUC_DU_AN>
     ```
   - Hoặc tải tệp ZIP về máy và giải nén.

---

## 2. Cách 1: Chạy bằng Apache NetBeans IDE (Khuyến nghị)

1. Mở **Apache NetBeans IDE** (phiên bản 19 trở lên).
2. Chọn menu **File** -> **Open Project...** (phím tắt `Ctrl + Shift + O`).
3. Điều hướng và chọn thư mục dự án vừa tải về -> Nhấn **Open Project**.
4. Tại cây thư mục **Projects** bên trái, chuột phải vào tên dự án -> chọn **Clean and Build**.
5. **Khởi chạy Server:**
   - Mở rộng **Source Packages** -> gói `server`.
   - Chuột phải vào tệp `Server.java` -> chọn **Run File** (phím tắt `Shift + F6`).
   - Cửa sổ Output thông báo: `[Server] Dang lang nghe tai cong 8888...`.
6. **Khởi chạy Client:**
   - Mở rộng **Source Packages** -> gói `client`.
   - Chuột phải vào tệp `ClientMain.java` -> chọn **Run File** (phím tắt `Shift + F6`).
   - Cửa sổ Đăng nhập sẽ xuất hiện. Nhập tên người chơi và bấm **Đăng nhập**.
   - *(Lặp lại thao tác Run File trên `ClientMain.java` để mở thêm 2 - 3 cửa sổ người chơi cùng lúc trên máy)*.

---

## 3. Cách 2: Biên dịch và Chạy bằng Dòng lệnh (CLI / Terminal)

Mở Terminal (Command Prompt / PowerShell trên Windows hoặc Terminal trên macOS / Linux) và di chuyển vào thư mục `source`:
```bash
cd source
```

### Bước 1: Biên dịch mã nguồn (chỉ làm 1 lần)
```bash
javac -encoding UTF-8 -sourcepath . server/Server.java client/ClientMain.java
```

### Bước 2: Khởi chạy Máy chủ (Server)
```bash
java -cp . server.Server
```
*(Màn hình sẽ hiển thị thông báo Server đã sẵn sàng kết nối tại cổng 8888)*.

### Bước 3: Khởi chạy Máy khách (Client)
Mở một (hoặc nhiều) cửa sổ Terminal mới, di chuyển vào thư mục `source` và chạy:
```bash
cd source
java -cp . client.ClientMain
```
*(Cửa sổ giao diện đăng nhập sẽ lập tức xuất hiện. Có thể mở nhiều cửa sổ terminal để mở nhiều người chơi cùng lúc)*.
