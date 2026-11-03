package client;

import common.Protocol;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Client giao diện dòng lệnh (CLI Console Client) để kiểm thử toàn diện
 * mọi tính năng Backend trước khi ghép với giao diện đồ họa Java Swing.
 */
public class TestConsoleClient {

    private final String host;
    private final int port;
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private volatile boolean isRunning = true;

    public TestConsoleClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

            System.out.println("==================================================");
            System.out.println("  KẾT NỐI SERVER THÀNH CÔNG TỚI " + host + ":" + port);
            System.out.println("  Gõ /help để xem danh sách các lệnh hỗ trợ.");
            System.out.println("==================================================");

            // Luồng lắng nghe dữ liệu từ Server
            Thread receiveThread = new Thread(this::listenServer);
            receiveThread.setDaemon(true);
            receiveThread.start();

            // Vòng lặp đọc input từ bàn phím người dùng
            Scanner scanner = new Scanner(System.in, "UTF-8");
            while (isRunning && scanner.hasNextLine()) {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) continue;

                if (input.equalsIgnoreCase("/quit")) {
                    System.out.println("Đang thoát chương trình...");
                    break;
                } else if (input.equalsIgnoreCase("/help")) {
                    printHelp();
                } else if (input.startsWith("/login ")) {
                    String username = input.substring(7).trim();
                    send(Protocol.LOGIN + "|" + username);
                } else if (input.equalsIgnoreCase("/rooms")) {
                    send(Protocol.GET_ROOMS);
                } else if (input.startsWith("/create ")) {
                    String roomId = input.substring(8).trim();
                    send(Protocol.CREATE_ROOM + "|" + roomId);
                } else if (input.startsWith("/join ")) {
                    String roomId = input.substring(6).trim();
                    send(Protocol.JOIN_ROOM + "|" + roomId);
                } else if (input.equalsIgnoreCase("/leave")) {
                    send(Protocol.LEAVE_ROOM);
                } else if (input.equalsIgnoreCase("/start")) {
                    send(Protocol.START_GAME);
                } else if (input.equalsIgnoreCase("/clear")) {
                    send(Protocol.CLEAR_CANVAS);
                } else if (input.startsWith("/draw ")) {
                    // Cú pháp: /draw x1 y1 x2 y2 #color size
                    String[] tokens = input.substring(6).trim().split("\\s+");
                    if (tokens.length >= 6) {
                        String payload = tokens[0] + "," + tokens[1] + "," + tokens[2] + "," + tokens[3] + "," + tokens[4] + "," + tokens[5];
                        send(Protocol.DRAW_STROKE + "|" + payload);
                    } else {
                        System.out.println("[LỖI CÚ PHÁP] /draw <x1> <y1> <x2> <y2> <#colorHex> <size>");
                    }
                } else {
                    // Mọi tin nhắn bình thường đều gửi qua giao thức CHAT_GUESS
                    send(Protocol.CHAT_GUESS + "|" + input);
                }
            }

        } catch (IOException e) {
            System.err.println("[LỖI KẾT NỐI] " + e.getMessage());
        } finally {
            close();
        }
    }

    private void listenServer() {
        try {
            String line;
            while (isRunning && (line = in.readLine()) != null) {
                parseServerMessage(line.trim());
            }
        } catch (IOException e) {
            if (isRunning) {
                System.out.println("[NGẮT KẾT NỐI] Mất kết nối tới máy chủ.");
            }
        }
    }

    private void parseServerMessage(String raw) {
        String[] parts = raw.split(Protocol.SPLIT_REGEX, -1);
        if (parts.length == 0) return;

        String action = parts[0];

        switch (action) {
            case Protocol.LOGIN_SUCCESS:
                System.out.println("\n>>> [ĐĂNG NHẬP THÀNH CÔNG] Xin chào, " + parts[1] + "!");
                break;
            case Protocol.LOGIN_FAILED:
                System.out.println("\n>>> [ĐĂNG NHẬP THẤT BẠI] " + (parts.length > 1 ? parts[1] : ""));
                break;
            case Protocol.ROOM_LIST:
                System.out.println("\n>>> [DANH SÁCH PHÒNG]");
                if (parts.length > 1 && !parts[1].isEmpty()) {
                    String[] rooms = parts[1].split(",");
                    for (String r : rooms) {
                        System.out.println("   * " + r);
                    }
                } else {
                    System.out.println("   (Chưa có phòng nào được tạo. Dùng /create <mã_phòng> để tạo mới)");
                }
                break;
            case Protocol.ROOM_UPDATE:
                if (parts.length >= 4) {
                    System.out.println("\n>>> [CẬP NHẬT PHÒNG " + parts[1] + "]");
                    System.out.println("    Thành viên: " + parts[2]);
                    System.out.println("    Chủ phòng : " + parts[3]);
                }
                break;
            case Protocol.ROUND_START:
                if (parts.length >= 3) {
                    System.out.println("\n==================================================");
                    System.out.println("  BẮT ĐẦU VÒNG " + parts[1] + " | NGƯỜI VẼ: " + parts[2]);
                    System.out.println("==================================================");
                }
                break;
            case Protocol.SECRET_WORD:
                if (parts.length >= 3) {
                    System.out.println("\n🎨 [BẠN LÀ NGƯỜI VẼ!]");
                    System.out.println("   Từ khóa cần vẽ: >>> " + parts[1] + " <<<");
                    System.out.println("   Chủ đề        : " + parts[2]);
                    System.out.println("   (Gõ /draw để gửi nét vẽ hoặc /clear để vẽ lại)");
                }
                break;
            case Protocol.WORD_HINT:
                if (parts.length >= 3) {
                    System.out.println("\n🔍 [GỢI Ý TỪ KHÓA]");
                    System.out.println("   Gợi ý : " + parts[1]);
                    System.out.println("   Chủ đề: " + parts[2]);
                    System.out.println("   (Nhập câu trả lời để đoán từ!)");
                }
                break;
            case Protocol.TIMER_TICK:
                if (parts.length >= 2) {
                    int sec = Integer.parseInt(parts[1]);
                    if (sec % 10 == 0 || sec <= 5) {
                        System.out.println("⏳ [THỜI GIAN] Còn " + sec + " giây!");
                    }
                }
                break;
            case Protocol.SYNC_STROKE:
                if (parts.length >= 2) {
                    System.out.println("✏️ [NHẬN NÉT VẼ] " + parts[1]);
                }
                break;
            case Protocol.CLEAR_CANVAS:
                System.out.println("🧹 [BẢNG VẼ] Người vẽ đã xóa toàn bộ bảng vẽ.");
                break;
            case Protocol.CORRECT_GUESS:
                if (parts.length >= 3) {
                    System.out.println("🎉 [CHÍNH XÁC!] Người chơi " + parts[1] + " đã đoán đúng từ khóa (+ " + parts[2] + " điểm)!");
                }
                break;
            case Protocol.ROUND_END:
                if (parts.length >= 3) {
                    System.out.println("\n🏁 [HẾT VÒNG] Từ khóa chính xác là: " + parts[1]);
                    System.out.println("   Bảng xếp hạng: " + parts[2]);
                }
                break;
            case Protocol.GAME_OVER:
                if (parts.length >= 3) {
                    System.out.println("\n🏆 [KẾT THÚC TRẬN ĐẤU]");
                    System.out.println("   Người chiến thắng: " + parts[1]);
                    System.out.println("   Điểm số chung cuộc: " + parts[2]);
                }
                break;
            case Protocol.CHAT_MESSAGE:
                if (parts.length >= 3) {
                    System.out.println("[" + parts[1] + "]: " + parts[2]);
                }
                break;
            default:
                System.out.println("[SERVER] " + raw);
                break;
        }
    }

    private synchronized void send(String msg) {
        if (!isRunning || out == null) return;
        try {
            out.write(msg);
            out.newLine();
            out.flush();
        } catch (IOException e) {
            System.err.println("[LỖI GỬI] " + e.getMessage());
        }
    }

    private void printHelp() {
        System.out.println("\n--- DANH SÁCH LỆNH CONSOLE CLIENT ---");
        System.out.println("  /login <tên>               : Đăng nhập vào hệ thống");
        System.out.println("  /rooms                     : Xem danh sách phòng");
        System.out.println("  /create <mã_phòng>         : Tạo một phòng chơi mới");
        System.out.println("  /join <mã_phòng>           : Tham gia vào phòng chơi");
        System.out.println("  /leave                     : Rời phòng chơi hiện tại");
        System.out.println("  /start                     : Bắt đầu ván đấu (dành cho Chủ phòng)");
        System.out.println("  /draw x1 y1 x2 y2 hex size : Giả lập vẽ nét (dành cho Người vẽ)");
        System.out.println("  /clear                     : Xóa bảng vẽ (dành cho Người vẽ)");
        System.out.println("  <tin_nhắn_bất_kỳ>          : Chat hoặc nhập từ đoán");
        System.out.println("  /quit                      : Thoát chương trình");
        System.out.println("-------------------------------------\n");
    }

    private void close() {
        isRunning = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }

    public static void main(String[] args) {
        String host = "localhost";
        int port = Protocol.DEFAULT_PORT;
        if (args.length > 0) host = args[0];
        if (args.length > 1) port = Integer.parseInt(args[1]);

        new TestConsoleClient(host, port).start();
    }
}
