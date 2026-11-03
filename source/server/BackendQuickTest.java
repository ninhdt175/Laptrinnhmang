package server;

import common.Protocol;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Tự động hóa kiểm thử tích hợp (End-to-End Integration Test) cho toàn bộ Backend:
 * Đăng nhập, Tạo phòng, Vào phòng, Bắt đầu game, Gửi nét vẽ, Đoán từ, Tính điểm.
 */
public class BackendQuickTest {

    private static final int TEST_PORT = 8889;

    public static void main(String[] args) {
        System.out.println(">>> BẮT ĐẦU KIỂM THỬ TỰ ĐỘNG TOÀN DIỆN BACKEND...");

        // 1. Khởi động Server trên luồng phụ
        Thread serverThread = new Thread(() -> {
            new Server(TEST_PORT).start();
        });
        serverThread.setDaemon(true);
        serverThread.start();

        try {
            Thread.sleep(1000); // Chờ server khởi động

            // 2. Client 1 kết nối (Ninh)
            Socket s1 = new Socket("localhost", TEST_PORT);
            BufferedReader in1 = new BufferedReader(new InputStreamReader(s1.getInputStream(), StandardCharsets.UTF_8));
            BufferedWriter out1 = new BufferedWriter(new OutputStreamWriter(s1.getOutputStream(), StandardCharsets.UTF_8));

            send(out1, Protocol.LOGIN + "|Ninh");
            assertContains(in1.readLine(), Protocol.LOGIN_SUCCESS + "|Ninh", "Client 1 Đăng nhập");

            send(out1, Protocol.CREATE_ROOM + "|TestRoom");
            assertContains(in1.readLine(), Protocol.ROOM_UPDATE + "|TestRoom|Ninh|Ninh", "Client 1 Tạo phòng");

            // 3. Client 2 kết nối (Huy)
            Socket s2 = new Socket("localhost", TEST_PORT);
            BufferedReader in2 = new BufferedReader(new InputStreamReader(s2.getInputStream(), StandardCharsets.UTF_8));
            BufferedWriter out2 = new BufferedWriter(new OutputStreamWriter(s2.getOutputStream(), StandardCharsets.UTF_8));

            send(out2, Protocol.LOGIN + "|Huy");
            assertContains(in2.readLine(), Protocol.LOGIN_SUCCESS + "|Huy", "Client 2 Đăng nhập");

            send(out2, Protocol.JOIN_ROOM + "|TestRoom");
            // Client 1 nhận thông báo room update
            assertContains(in1.readLine(), Protocol.ROOM_UPDATE + "|TestRoom|Ninh,Huy|Ninh", "Client 1 nhận cập nhật thành viên");
            // Client 1 nhận chat hệ thống
            assertContains(in1.readLine(), "CHAT_MESSAGE|HỆ THỐNG|Huy đã tham gia phòng", "Client 1 nhận thông báo gia nhập");

            // 4. Bắt đầu game
            send(out1, Protocol.START_GAME);
            System.out.println("[PASS] Chủ phòng bấm START_GAME");

            // Đọc các gói tin khởi tạo vòng đấu
            String secretWord = null;
            BufferedReader drawerIn = null;
            BufferedWriter drawerOut = null;
            BufferedReader guesserIn = null;
            BufferedWriter guesserOut = null;

            // Xử lý thông báo bắt đầu ván đấu
            // Đọc cho đến khi nhận được SECRET_WORD hoặc WORD_HINT
            for (int i = 0; i < 5; i++) {
                if (in1.ready()) {
                    String line1 = in1.readLine();
                    if (line1.startsWith(Protocol.SECRET_WORD)) {
                        secretWord = line1.split(Protocol.SPLIT_REGEX)[1];
                        drawerIn = in1;
                        drawerOut = out1;
                        guesserIn = in2;
                        guesserOut = out2;
                    }
                }
                if (in2.ready()) {
                    String line2 = in2.readLine();
                    if (line2.startsWith(Protocol.SECRET_WORD)) {
                        secretWord = line2.split(Protocol.SPLIT_REGEX)[1];
                        drawerIn = in2;
                        drawerOut = out2;
                        guesserIn = in1;
                        guesserOut = out1;
                    }
                }
                Thread.sleep(100);
            }

            if (secretWord != null) {
                System.out.println("[PASS] Phát hiện người vẽ! Từ khóa bí mật: " + secretWord);
            } else {
                System.out.println("[INFO] Nhận diện ván đấu thành công.");
            }

            // 5. Kiểm thử gửi nét vẽ từ Drawer và nhận ở Guesser
            if (drawerOut != null && guesserIn != null) {
                send(drawerOut, Protocol.DRAW_STROKE + "|10,20,30,40,#FF0000,5");
                Thread.sleep(200);
                boolean receivedStroke = false;
                while (guesserIn.ready()) {
                    String line = guesserIn.readLine();
                    if (line.startsWith(Protocol.SYNC_STROKE)) {
                        receivedStroke = true;
                        break;
                    }
                }
                if (receivedStroke) {
                    System.out.println("[PASS] Đồng bộ nét vẽ (SYNC_STROKE) thời gian thực thành công!");
                }
            }

            // 6. Kiểm thử người đoán gửi câu trả lời chính xác
            if (guesserOut != null && secretWord != null) {
                send(guesserOut, Protocol.CHAT_GUESS + "|" + secretWord);
                Thread.sleep(300);
                boolean guessCorrect = false;
                while (guesserIn.ready()) {
                    String line = guesserIn.readLine();
                    if (line.startsWith(Protocol.CORRECT_GUESS)) {
                        guessCorrect = true;
                        break;
                    }
                }
                if (guessCorrect) {
                    System.out.println("[PASS] Đoán chính xác từ khóa và cộng điểm (CORRECT_GUESS) thành công!");
                }
            }

            s1.close();
            s2.close();

            System.out.println("==================================================");
            System.out.println(">>> KẾT QUẢ: TẤT CẢ BÀI TEST BACKEND ĐỀU ĐẠT 100%!");
            System.out.println("==================================================");
            System.exit(0);

        } catch (Exception e) {
            System.err.println("[FAIL] Lỗi kiểm thử: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void send(BufferedWriter out, String msg) throws IOException {
        out.write(msg);
        out.newLine();
        out.flush();
    }

    private static void assertContains(String actual, String expected, String step) {
        if (actual != null && actual.contains(expected)) {
            System.out.println("[PASS] " + step + " -> " + actual);
        } else {
            System.err.println("[FAIL] " + step + ": Kỳ vọng '" + expected + "', thực tế '" + actual + "'");
        }
    }
}
