package server;

import common.Protocol;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Lớp chủ Server quản lý ServerSocket và phân bổ luồng cho các Client.
 */
public class Server {

    private final int port;
    private ServerSocket serverSocket;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private volatile boolean isRunning = true;

    // Quản lý các username đang trực tuyến (không phân biệt hoa thường)
    private static final Set<String> ACTIVE_USERNAMES = ConcurrentHashMap.newKeySet();

    public Server(int port) {
        this.port = port;
    }

    public static boolean isUsernameTaken(String username) {
        if (username == null) return true;
        return ACTIVE_USERNAMES.contains(username.trim().toLowerCase());
    }

    public static void registerUsername(String username) {
        if (username != null) {
            ACTIVE_USERNAMES.add(username.trim().toLowerCase());
        }
    }

    public static void unregisterUsername(String username) {
        if (username != null) {
            ACTIVE_USERNAMES.remove(username.trim().toLowerCase());
        }
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("=================================================");
            System.out.println("  MÁY CHỦ TRÒ CHƠI: VẼ HÌNH ĐOÁN CHỮ (PICTIONARY)");
            System.out.println("  Cổng lắng nghe (Port): " + port);
            System.out.println("  Trạng thái: SẴN SÀNG KẾT NỐI");
            System.out.println("=================================================");

            // Đăng ký hook dọn dẹp khi tắt server
            Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("[KẾT NỐI MỚI] Từ IP: " + clientSocket.getRemoteSocketAddress());
                    ClientHandler handler = new ClientHandler(clientSocket);
                    threadPool.submit(handler);
                } catch (IOException e) {
                    if (!isRunning) {
                        break;
                    }
                    System.err.println("[ERROR] Lỗi chấp nhận kết nối: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("[FATAL] Không thể khởi động Server trên cổng " + port + ": " + e.getMessage());
        } finally {
            stop();
        }
    }

    public synchronized void stop() {
        if (!isRunning) return;
        isRunning = false;
        System.out.println("[SERVER] Đang dừng máy chủ...");

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ignored) {}

        threadPool.shutdownNow();
        System.out.println("[SERVER] Máy chủ đã tắt hoàn toàn.");
    }

    public static void main(String[] args) {
        int port = Protocol.DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Cổng không hợp lệ, sử dụng mặc định: " + Protocol.DEFAULT_PORT);
            }
        }
        new Server(port).start();
    }
}
