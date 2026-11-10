package client.network;

import common.Protocol;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ClientSocketManager {
    private static final ClientSocketManager INSTANCE = new ClientSocketManager();

    private final CopyOnWriteArrayList<ServerListener> listeners =
            new CopyOnWriteArrayList<>();

    private volatile Socket socket;
    private volatile BufferedReader reader;
    private volatile BufferedWriter writer;
    private volatile boolean connected;
    private volatile String currentUsername;
    private volatile boolean disconnectNotified = true;

    private ClientSocketManager() { }

    public static ClientSocketManager getInstance() {
        return INSTANCE;
    }
    public synchronized boolean connect(String host, int port) {
        if (connected) {
            return true;
        }
        closeResources();
        try {
            Socket newSocket = new Socket();
            newSocket.connect(new InetSocketAddress(host, port), 5000);
            newSocket.setKeepAlive(true);
            newSocket.setTcpNoDelay(true);
            socket = newSocket;
            reader = new BufferedReader(new InputStreamReader(
                    newSocket.getInputStream(), StandardCharsets.UTF_8));
            writer = new BufferedWriter(new OutputStreamWriter(
                    newSocket.getOutputStream(), StandardCharsets.UTF_8));

            connected = true;
            disconnectNotified = false;
            Thread receiver = new Thread(this::receiveMessages, "Client-Receiver");
            receiver.setDaemon(true);
            receiver.start();
            return true;
        } catch (IOException ex) {
            closeResources();
            connected = false;
            return false;
        }
    }
    public synchronized boolean sendPacket(String action, String... params) {
        if (!connected || writer == null || action == null || action.isBlank()) {
            return false;
        }
        StringBuilder packet = new StringBuilder(action.trim());
        if (params != null) {
            for (String param : params) {
                if (param == null || param.contains(Protocol.DELIMITER)
                        || param.contains("\n") || param.contains("\r")) {
                    return false;
                }
                packet.append(Protocol.DELIMITER).append(param);
            }
        }
        try {
            writer.write(packet.toString());
            writer.newLine();
            writer.flush();
            return true;
        } catch (IOException ex) {
            closeConnection();
            return false;
        }
    }
    private void receiveMessages() {
        BufferedReader localReader = reader;
        try {
            String message;
            while (connected && localReader != null && (message = localReader.readLine()) != null) {
                if (message.isBlank()) {
                    continue;
                }

                String[] parts = message.split(Protocol.SPLIT_REGEX, -1);
                String action = parts[0].trim();
                String[] params = Arrays.copyOfRange(parts, 1, parts.length);

                for (ServerListener listener : listeners) {
                    try {
                        listener.onMessageReceived(action, params);
                    } catch (RuntimeException ex) {
                        System.err.println("[CLIENT] Listener xử lý packet lỗi: " + ex.getMessage());
                    }
                }
            }
        } catch (IOException ex) {
            if (connected) {
                System.err.println("[CLIENT] Mất kết nối tới Server: " + ex.getMessage());
            }
        } finally {
            connected = false;
            closeResources();
            notifyDisconnected();
        }
    }
    private synchronized void notifyDisconnected() {
        if (disconnectNotified) {
            return;
        }
        disconnectNotified = true;
        for (ServerListener listener : listeners) {
            try {
                listener.onDisconnected();
            } catch (RuntimeException ex) {
                System.err.println("[CLIENT] Lỗi callback ngắt kết nối: " + ex.getMessage());
            }
        }
    }

    public void addListener(ServerListener listener) {
        if (listener != null) {
            listeners.addIfAbsent(listener);
        }
    }

    public void removeListener(ServerListener listener) {
        listeners.remove(listener);
    }

    public void setCurrentUsername(String username) {
        currentUsername = username;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public boolean isConnected() {
        return connected;
    }
    public synchronized void closeConnection() {
        connected = false;
        closeResources();
        notifyDisconnected();
    }
    private synchronized void closeResources() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) { }
        socket = null;
        reader = null;
        writer = null;
    }
}
