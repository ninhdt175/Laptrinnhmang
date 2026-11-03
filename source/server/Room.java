package server;

import common.Player;
import common.Protocol;
import common.Stroke;

import java.util.*;
import java.util.concurrent.*;

/**
 * Quản lý phòng chơi và vòng lặp trò chơi (Game Loop) Vẽ hình đoán chữ.
 */
public class Room {

    public enum State {
        WAITING,
        PLAYING,
        ROUND_END,
        GAME_OVER
    }

    private final String id;
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private final List<Stroke> currentStrokes = new CopyOnWriteArrayList<>();

    private volatile ClientHandler host;
    private volatile State state = State.WAITING;

    private int currentRound = 0;
    private int totalRounds = 3;
    private int drawerIndex = -1;

    private WordDictionary.Word secretWord;
    private ScheduledExecutorService timerService;
    private ScheduledFuture<?> timerTask;
    private volatile int timeLeft = 0;

    private static final int ROUND_TIME_SECONDS = 60;
    private static final int ROUND_REST_SECONDS = 6;
    private static final int MAX_PLAYERS = 8;

    public Room(String id, ClientHandler host) {
        this.id = id;
        this.host = host;
        this.clients.add(host);
        host.getPlayer().setHost(true);
        host.setRoom(this);
    }

    public String getId() { return id; }
    public State getState() { return state; }
    public ClientHandler getHost() { return host; }
    public List<ClientHandler> getClients() { return clients; }
    public int getPlayerCount() { return clients.size(); }
    public boolean isFull() { return clients.size() >= MAX_PLAYERS; }

    /**
     * Thêm người chơi vào phòng
     */
    public synchronized boolean addClient(ClientHandler client) {
        if (isFull()) return false;
        if (!clients.contains(client)) {
            clients.add(client);
            client.getPlayer().setHost(false);
            client.getPlayer().setScore(0);
            client.setRoom(this);

            broadcastRoomUpdate();
            broadcast(Protocol.CHAT_MESSAGE + "|HỆ THỐNG|" + client.getPlayer().getUsername() + " đã tham gia phòng.");

            // Nếu đang trong trận, gửi các nét vẽ hiện tại cho người mới vào xem
            if (state == State.PLAYING) {
                for (Stroke stroke : currentStrokes) {
                    client.send(Protocol.SYNC_STROKE + "|" + stroke.toProtocolString());
                }
                client.send(Protocol.WORD_HINT + "|" + WordDictionary.generateHint(secretWord.getText()) + "|" + secretWord.getCategory());
                client.send(Protocol.TIMER_TICK + "|" + timeLeft);
            }
        }
        return true;
    }

    /**
     * Xóa người chơi khỏi phòng
     */
    public synchronized void removeClient(ClientHandler client) {
        if (clients.remove(client)) {
            client.setRoom(null);
            broadcast(Protocol.CHAT_MESSAGE + "|HỆ THỐNG|" + client.getPlayer().getUsername() + " đã rời phòng.");

            if (clients.isEmpty()) {
                stopTimer();
                return;
            }

            // Chuyển quyền Host nếu Host rời phòng
            if (client == host) {
                host = clients.get(0);
                host.getPlayer().setHost(true);
                broadcast(Protocol.CHAT_MESSAGE + "|HỆ THỐNG|" + host.getPlayer().getUsername() + " đã trở thành chủ phòng mới.");
            }

            // Nếu người vẽ rời phòng khi đang chơi -> kết thúc sớm vòng hiện tại
            if (state == State.PLAYING && client.getPlayer().isDrawer()) {
                broadcast(Protocol.CHAT_MESSAGE + "|HỆ THỐNG|Người vẽ đã thoát game! Chuyển sang vòng tiếp theo...");
                endRound();
            } else if (state == State.PLAYING) {
                // Kiểm tra xem tất cả người đoán còn lại đã đoán xong chưa
                checkAllGuessed();
            }

            broadcastRoomUpdate();
        }
    }

    /**
     * Bắt đầu trò chơi
     */
    public synchronized boolean startGame(ClientHandler requester) {
        if (requester != host) {
            requester.send(Protocol.CHAT_MESSAGE + "|HỆ THỐNG|Chỉ chủ phòng mới có quyền bắt đầu trận đấu!");
            return false;
        }
        if (clients.size() < 2) {
            requester.send(Protocol.CHAT_MESSAGE + "|HỆ THỐNG|Cần tối thiểu 2 người chơi để bắt đầu!");
            return false;
        }
        if (state == State.PLAYING) {
            return false;
        }

        // Đặt lại điểm số của tất cả người chơi
        for (ClientHandler ch : clients) {
            ch.getPlayer().setScore(0);
        }

        currentRound = 0;
        drawerIndex = -1;
        totalRounds = Math.max(3, clients.size()); // Mỗi người được vẽ ít nhất 1 lần

        broadcast(Protocol.CHAT_MESSAGE + "|HỆ THỐNG|Trận đấu bắt đầu! Tổng số vòng: " + totalRounds);
        startNextRound();
        return true;
    }

    /**
     * Bắt đầu vòng chơi tiếp theo
     */
    private synchronized void startNextRound() {
        currentRound++;
        if (currentRound > totalRounds || clients.size() < 2) {
            endGame();
            return;
        }

        state = State.PLAYING;
        currentStrokes.clear();

        // Xoay vòng người vẽ
        drawerIndex = (drawerIndex + 1) % clients.size();
        ClientHandler drawer = clients.get(drawerIndex);

        // Reset trạng thái người chơi
        for (ClientHandler ch : clients) {
            ch.getPlayer().resetForNewRound();
        }
        drawer.getPlayer().setDrawer(true);

        // Lấy từ khóa bí mật
        secretWord = WordDictionary.getRandomWord();

        // Thông báo cho cả phòng
        broadcast(Protocol.CLEAR_CANVAS);
        broadcast(Protocol.ROUND_START + "|" + currentRound + "|" + drawer.getPlayer().getUsername());

        // Gửi từ khóa cho người vẽ, gợi ý cho người đoán
        drawer.send(Protocol.SECRET_WORD + "|" + secretWord.getText() + "|" + secretWord.getCategory());
        String hintPattern = WordDictionary.generateHint(secretWord.getText());
        for (ClientHandler ch : clients) {
            if (ch != drawer) {
                ch.send(Protocol.WORD_HINT + "|" + hintPattern + "|" + secretWord.getCategory());
            }
        }

        // Bắt đầu đếm ngược thời gian
        timeLeft = ROUND_TIME_SECONDS;
        startTimer();
    }

    /**
     * Bộ đếm thời gian cho mỗi vòng
     */
    private void startTimer() {
        stopTimer();
        timerService = Executors.newSingleThreadScheduledExecutor();
        timerTask = timerService.scheduleAtFixedRate(() -> {
            try {
                timeLeft--;
                broadcast(Protocol.TIMER_TICK + "|" + timeLeft);

                if (timeLeft <= 0) {
                    stopTimer();
                    endRound();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    private synchronized void stopTimer() {
        if (timerTask != null) {
            timerTask.cancel(true);
            timerTask = null;
        }
        if (timerService != null && !timerService.isShutdown()) {
            timerService.shutdownNow();
            timerService = null;
        }
    }

    /**
     * Kết thúc vòng chơi
     */
    public synchronized void endRound() {
        if (state != State.PLAYING) return;
        state = State.ROUND_END;
        stopTimer();

        String wordAnswer = secretWord != null ? secretWord.getText() : "Không rõ";
        String leaderboard = getLeaderboardString();

        broadcast(Protocol.ROUND_END + "|" + wordAnswer + "|" + leaderboard);
        broadcast(Protocol.CHAT_MESSAGE + "|HỆ THỐNG|Vòng " + currentRound + " kết thúc! Từ khóa là: " + wordAnswer);

        // Nghỉ vài giây trước khi sang vòng mới
        timerService = Executors.newSingleThreadScheduledExecutor();
        timerService.schedule(() -> {
            startNextRound();
        }, ROUND_REST_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Kết thúc toàn bộ ván đấu
     */
    private synchronized void endGame() {
        state = State.GAME_OVER;
        stopTimer();

        // Tìm người thắng có điểm cao nhất
        ClientHandler winner = null;
        int maxScore = -1;
        for (ClientHandler ch : clients) {
            if (ch.getPlayer().getScore() > maxScore) {
                maxScore = ch.getPlayer().getScore();
                winner = ch;
            }
        }

        String winnerName = winner != null ? winner.getPlayer().getUsername() : "Không ai";
        String finalScores = getLeaderboardString();

        broadcast(Protocol.GAME_OVER + "|" + winnerName + "|" + finalScores);
        broadcast(Protocol.CHAT_MESSAGE + "|HỆ THỐNG|Trận đấu đã kết thúc! Người chiến thắng là: " + winnerName + " (" + maxScore + " điểm)");

        state = State.WAITING;
        broadcastRoomUpdate();
    }

    /**
     * Xử lý nét vẽ từ Drawer
     */
    public void handleDrawStroke(ClientHandler sender, Stroke stroke) {
        if (state != State.PLAYING) return;
        if (!sender.getPlayer().isDrawer()) return;

        currentStrokes.add(stroke);
        String msg = Protocol.SYNC_STROKE + "|" + stroke.toProtocolString();
        for (ClientHandler ch : clients) {
            if (ch != sender) {
                ch.send(msg);
            }
        }
    }

    /**
     * Xử lý xóa toàn bộ bảng vẽ
     */
    public void handleClearCanvas(ClientHandler sender) {
        if (state != State.PLAYING) return;
        if (!sender.getPlayer().isDrawer()) return;

        currentStrokes.clear();
        broadcast(Protocol.CLEAR_CANVAS);
    }

    /**
     * Xử lý tin nhắn chat và đoán chữ
     */
    public synchronized void handleChatMessage(ClientHandler sender, String content) {
        if (content == null || content.trim().isEmpty()) return;
        String text = content.trim();

        // Nếu đang trong trận đấu
        if (state == State.PLAYING) {
            // 1. Nếu người gửi là người vẽ
            if (sender.getPlayer().isDrawer()) {
                // Kiểm tra xem người vẽ có cố tình gõ từ khóa lộ liễu không
                if (secretWord != null && WordDictionary.checkMatch(text, secretWord.getText())) {
                    sender.send(Protocol.CHAT_MESSAGE + "|HỆ THỐNG|Bạn là người vẽ, không được phép tiết lộ từ khóa!");
                    return;
                }
                broadcast(Protocol.CHAT_MESSAGE + "|" + sender.getPlayer().getUsername() + " (Người vẽ)|" + text);
                return;
            }

            // 2. Nếu người đoán đã đoán đúng trước đó trong vòng này
            if (sender.getPlayer().hasGuessedCorrectly()) {
                sender.send(Protocol.CHAT_MESSAGE + "|HỆ THỐNG|Bạn đã đoán đúng từ khóa vòng này rồi! Vui lòng giữ bí mật.");
                return;
            }

            // 3. Kiểm tra xem có đoán đúng từ khóa không
            if (secretWord != null && WordDictionary.checkMatch(text, secretWord.getText())) {
                sender.getPlayer().setHasGuessedCorrectly(true);

                // Tính điểm theo thời gian còn lại (nhanh hơn được nhiều điểm hơn)
                int pointsAwarded = Math.max(30, 40 + (timeLeft * 2));
                sender.getPlayer().addScore(pointsAwarded);

                // Người vẽ cũng được cộng điểm hỗ trợ
                ClientHandler drawer = getDrawer();
                if (drawer != null) {
                    drawer.getPlayer().addScore(20);
                }

                broadcast(Protocol.CORRECT_GUESS + "|" + sender.getPlayer().getUsername() + "|" + pointsAwarded);
                broadcast(Protocol.CHAT_MESSAGE + "|HỆ THỐNG|" + sender.getPlayer().getUsername() + " đã đoán chính xác từ khóa! (+" + pointsAwarded + "đ)");

                checkAllGuessed();
                return;
            }
        }

        // Tin nhắn chat thông thường
        broadcast(Protocol.CHAT_MESSAGE + "|" + sender.getPlayer().getUsername() + "|" + text);
    }

    /**
     * Kiểm tra xem tất cả người đoán đã đoán trúng từ chưa
     */
    private void checkAllGuessed() {
        boolean allGuessed = true;
        int guesserCount = 0;

        for (ClientHandler ch : clients) {
            if (!ch.getPlayer().isDrawer()) {
                guesserCount++;
                if (!ch.getPlayer().hasGuessedCorrectly()) {
                    allGuessed = false;
                    break;
                }
            }
        }

        // Nếu tất cả người đoán đã đoán xong -> kết thúc vòng sớm
        if (guesserCount > 0 && allGuessed) {
            broadcast(Protocol.CHAT_MESSAGE + "|HỆ THỐNG|Tất cả người chơi đều đã đoán đúng từ khóa!");
            endRound();
        }
    }

    public ClientHandler getDrawer() {
        for (ClientHandler ch : clients) {
            if (ch.getPlayer().isDrawer()) return ch;
        }
        return null;
    }

    /**
     * Gửi cập nhật thông tin phòng tới tất cả thành viên trong phòng
     */
    public void broadcastRoomUpdate() {
        StringBuilder sbPlayers = new StringBuilder();
        for (int i = 0; i < clients.size(); i++) {
            sbPlayers.append(clients.get(i).getPlayer().getUsername());
            if (i < clients.size() - 1) sbPlayers.append(",");
        }
        String hostName = host != null ? host.getPlayer().getUsername() : "";
        String msg = Protocol.ROOM_UPDATE + "|" + id + "|" + sbPlayers.toString() + "|" + hostName;
        broadcast(msg);
    }

    /**
     * Tạo chuỗi bảng xếp hạng điểm: "user1:100,user2:80,user3:50"
     */
    public String getLeaderboardString() {
        List<ClientHandler> sorted = new ArrayList<>(clients);
        sorted.sort((a, b) -> Integer.compare(b.getPlayer().getScore(), a.getPlayer().getScore()));

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sorted.size(); i++) {
            Player p = sorted.get(i).getPlayer();
            sb.append(p.getUsername()).append(":").append(p.getScore());
            if (i < sorted.size() - 1) sb.append(",");
        }
        return sb.toString();
    }

    /**
     * Gửi tin nhắn tới mọi client trong phòng
     */
    public void broadcast(String message) {
        for (ClientHandler ch : clients) {
            ch.send(message);
        }
    }

    /**
     * Gửi tin nhắn tới tất cả mọi người trừ người gửi
     */
    public void broadcastExcept(ClientHandler sender, String message) {
        for (ClientHandler ch : clients) {
            if (ch != sender) {
                ch.send(message);
            }
        }
    }
}
