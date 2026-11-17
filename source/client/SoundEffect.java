package client;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class SoundEffect {
    private static final String SOUND_PATH = "/resources/";
    //Quản lý luồng phát âm thanh riêng
    private static final ExecutorService SOUND_EXECUTOR =
            Executors.newFixedThreadPool(2, runnable -> {
                Thread thread = new Thread(runnable, "Game-Sound");
                thread.setDaemon(true);
                return thread;
            });

    private SoundEffect() {
        
    }
    public static void playCorrectSound() {
        playSound("correct.wav");
    }
    
    public static void playTimeUpSound() {
        playSound("timeout.wav");
    }

    // Phát âm thanh từ file WAV
    private static void playSound(String fileName) {
        SOUND_EXECUTOR.execute(() -> {
            try (InputStream resource =
                         SoundEffect.class.getResourceAsStream(
                                 SOUND_PATH + fileName)) {

                if (resource == null) {
                    System.err.println(
                            "Không tìm thấy file âm thanh: " + fileName);
                    return;
                }
                try (AudioInputStream audioStream =
                             AudioSystem.getAudioInputStream(
                                     new BufferedInputStream(resource))) {

                    Clip clip = AudioSystem.getClip();
                    clip.open(audioStream);
                    clip.addLineListener(event -> {
                        if (event.getType() == LineEvent.Type.STOP) {
                            clip.close();
                        }
                    });

                    clip.start();
                }
            } catch (Exception e) {
                System.err.println(
                        "Không thể phát âm thanh " + fileName
                                + ": " + e.getMessage());
            }
        });
    }
}
