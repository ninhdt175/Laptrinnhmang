package common;

/**
 * Danh mục các hằng số quy chuẩn giao thức truyền thông mạng TCP.
 * Định dạng gói tin: ACTION|PARAM1|PARAM2|...
 */
public class Protocol {
    public static final int DEFAULT_PORT = 8888;
    public static final String DELIMITER = "|";
    public static final String SPLIT_REGEX = "\\|";

    // 1. Quản lý Đăng nhập & Kết nối
    public static final String LOGIN = "LOGIN";                   // C -> S: LOGIN|username
    public static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";   // S -> C: LOGIN_SUCCESS|username
    public static final String LOGIN_FAILED = "LOGIN_FAILED";     // S -> C: LOGIN_FAILED|reason

    // 2. Quản lý Phòng chơi (Lobby / Rooms)
    public static final String GET_ROOMS = "GET_ROOMS";           // C -> S: GET_ROOMS
    public static final String ROOM_LIST = "ROOM_LIST";           // S -> C: ROOM_LIST|room1:players,room2:players...
    public static final String CREATE_ROOM = "CREATE_ROOM";       // C -> S: CREATE_ROOM|roomId
    public static final String JOIN_ROOM = "JOIN_ROOM";           // C -> S: JOIN_ROOM|roomId
    public static final String LEAVE_ROOM = "LEAVE_ROOM";         // C -> S: LEAVE_ROOM
    public static final String ROOM_UPDATE = "ROOM_UPDATE";       // S -> C: ROOM_UPDATE|roomId|player1,player2...|hostUsername

    // 3. Vòng đời Trò chơi (Game Flow)
    public static final String START_GAME = "START_GAME";         // C -> S: START_GAME
    public static final String ROUND_START = "ROUND_START";       // S -> C: ROUND_START|roundNumber|drawerUsername
    public static final String SECRET_WORD = "SECRET_WORD";       // S -> C (chỉ gửi Drawer): SECRET_WORD|word|category
    public static final String WORD_HINT = "WORD_HINT";           // S -> C (gửi Guessers): WORD_HINT|hintPattern|category
    public static final String TIMER_TICK = "TIMER_TICK";         // S -> C: TIMER_TICK|secondsRemaining
    public static final String ROUND_END = "ROUND_END";           // S -> C: ROUND_END|secretWord|leaderboardData
    public static final String GAME_OVER = "GAME_OVER";           // S -> C: GAME_OVER|winnerUsername|finalScores

    // 4. Đồng bộ Bảng vẽ (Drawing)
    public static final String DRAW_STROKE = "DRAW_STROKE";       // C (Drawer) -> S: DRAW_STROKE|x1,y1,x2,y2,colorHex,size
    public static final String SYNC_STROKE = "SYNC_STROKE";       // S -> C (Guessers): SYNC_STROKE|x1,y1,x2,y2,colorHex,size
    public static final String CLEAR_CANVAS = "CLEAR_CANVAS";     // C -> S -> C: CLEAR_CANVAS

    // 5. Khung Chat & Đoán chữ
    public static final String CHAT_GUESS = "CHAT_GUESS";         // C -> S: CHAT_GUESS|message
    public static final String CHAT_MESSAGE = "CHAT_MESSAGE";     // S -> C: CHAT_MESSAGE|sender|message
    public static final String CORRECT_GUESS = "CORRECT_GUESS";   // S -> C: CORRECT_GUESS|guesserUsername|pointsAwarded
}
