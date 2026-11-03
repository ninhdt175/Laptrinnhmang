package common;

/**
 * Mô hình thông tin người chơi trong phòng đấu.
 */
public class Player {
    private String username;
    private int score;
    private boolean isHost;
    private boolean isDrawer;
    private boolean hasGuessedCorrectly;

    public Player(String username) {
        this.username = username;
        this.score = 0;
        this.isHost = false;
        this.isDrawer = false;
        this.hasGuessedCorrectly = false;
    }

    public String getUsername() { return username; }
    public int getScore() { return score; }
    public void addScore(int points) { this.score += points; }
    public void setScore(int score) { this.score = score; }

    public boolean isHost() { return isHost; }
    public void setHost(boolean host) { isHost = host; }

    public boolean isDrawer() { return isDrawer; }
    public void setDrawer(boolean drawer) { isDrawer = drawer; }

    public boolean hasGuessedCorrectly() { return hasGuessedCorrectly; }
    public void setHasGuessedCorrectly(boolean guessed) { this.hasGuessedCorrectly = guessed; }

    public void resetForNewRound() {
        this.isDrawer = false;
        this.hasGuessedCorrectly = false;
    }

    @Override
    public String toString() {
        return username + " (" + score + "đ)";
    }
}
