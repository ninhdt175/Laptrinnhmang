package server;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * Kho từ vựng tiếng Việt phục vụ trò chơi Vẽ hình đoán chữ.
 */
public class WordDictionary {

    public static class Word {
        private final String text;
        private final String category;

        public Word(String text, String category) {
            this.text = text.toUpperCase();
            this.category = category;
        }

        public String getText() { return text; }
        public String getCategory() { return category; }
    }

    private static final List<Word> WORDS = new ArrayList<>();
    private static final Random RANDOM = new Random();

    static {
        // Động vật
        WORDS.add(new Word("CON MÈO", "Động vật"));
        WORDS.add(new Word("CON CHÓ", "Động vật"));
        WORDS.add(new Word("CON VOI", "Động vật"));
        WORDS.add(new Word("CON HỔ", "Động vật"));
        WORDS.add(new Word("CON GÀ", "Động vật"));
        WORDS.add(new Word("CÁ HEO", "Động vật"));
        WORDS.add(new Word("CON HƯƠU", "Động vật"));
        WORDS.add(new Word("CON THỎ", "Động vật"));
        WORDS.add(new Word("CON GẤU", "Động vật"));
        WORDS.add(new Word("CON KHỈ", "Động vật"));

        // Đồ vật
        WORDS.add(new Word("CÁI BÀN", "Đồ vật"));
        WORDS.add(new Word("CÁI GHẾ", "Đồ vật"));
        WORDS.add(new Word("MÁY TÍNH", "Đồ vật"));
        WORDS.add(new Word("ĐIỆN THOẠI", "Đồ vật"));
        WORDS.add(new Word("CÂY BÚT", "Đồ vật"));
        WORDS.add(new Word("QUYỂN SÁCH", "Đồ vật"));
        WORDS.add(new Word("CHIẾC NÓN", "Đồ vật"));
        WORDS.add(new Word("CÁI KÍNH", "Đồ vật"));
        WORDS.add(new Word("ĐỒNG HỒ", "Đồ vật"));
        WORDS.add(new Word("CHIẾC Ô", "Đồ vật"));

        // Phương tiện giao thông
        WORDS.add(new Word("XE ĐẠP", "Phương tiện"));
        WORDS.add(new Word("XE MÁY", "Phương tiện"));
        WORDS.add(new Word("Ô TÔ", "Phương tiện"));
        WORDS.add(new Word("MÁY BAY", "Phương tiện"));
        WORDS.add(new Word("TÀU THỦY", "Phương tiện"));
        WORDS.add(new Word("TÀU HỎA", "Phương tiện"));
        WORDS.add(new Word("XE BUÝT", "Phương tiện"));
        WORDS.add(new Word("TRỰC THĂNG", "Phương tiện"));

        // Trái cây / Đồ ăn
        WORDS.add(new Word("DƯA HẤU", "Ẩm thực"));
        WORDS.add(new Word("QUẢ CHUỐI", "Ẩm thực"));
        WORDS.add(new Word("QUẢ TÁO", "Ẩm thực"));
        WORDS.add(new Word("QUẢ CAM", "Ẩm thực"));
        WORDS.add(new Word("BÁNH MÌ", "Ẩm thực"));
        WORDS.add(new Word("KEM", "Ẩm thực"));
        WORDS.add(new Word("TRỨNG", "Ẩm thực"));
        WORDS.add(new Word("CÀ RỐT", "Ẩm thực"));
    }

    /**
     * Lấy ngẫu nhiên một từ khóa bí mật
     */
    public static Word getRandomWord() {
        return WORDS.get(RANDOM.nextInt(WORDS.size()));
    }

    /**
     * Tạo gợi ý gạch dưới hiển thị cho người đoán (ví dụ: "CON MÈO" -> "_ _ _   _ _ _")
     */
    public static String generateHint(String word) {
        if (word == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (Character.isWhitespace(c)) {
                sb.append("   "); // Khoảng trống giữa các từ
            } else {
                sb.append("_ ");
            }
        }
        return sb.toString().trim();
    }

    /**
     * Kiểm tra từ đoán có khớp với từ khóa bí mật hay không.
     * Hỗ trợ so sánh linh hoạt: khớp có dấu hoặc khớp không dấu.
     */
    public static boolean checkMatch(String guess, String secret) {
        if (guess == null || secret == null) return false;
        String g = guess.trim().toUpperCase();
        String s = secret.trim().toUpperCase();

        // 1. So khớp trực tiếp (có dấu)
        if (g.equals(s)) return true;

        // 2. So khớp không dấu
        String gNoAccent = removeAccents(g);
        String sNoAccent = removeAccents(s);
        return gNoAccent.equals(sNoAccent);
    }

    /**
     * Khử dấu tiếng Việt để tăng tính thân thiện khi người chơi gõ nhanh không dấu
     */
    public static String removeAccents(String str) {
        if (str == null) return "";
        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString)
                .replaceAll("")
                .replace('Đ', 'D')
                .replace('đ', 'd');
    }
}
