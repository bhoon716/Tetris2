package kr.ac.jbnu.se.tetris;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
public class Bgm {
    private Clip audioClip;
    private static final String AUDIO_FILE_PATH = "src\\main\\resources\\bgm.wav";
    private FloatControl volumeControl;
    private static final Logger logger = Logger.getLogger(Bgm.class.getName());

    public Bgm() {
        try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(AUDIO_FILE_PATH))) {
            // 오디오 포맷을 얻어옵니다.
            AudioFormat format = audioStream.getFormat();

            // 데이터 라인을 열고 시작합니다.
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            audioClip = (Clip) AudioSystem.getLine(info);
            audioClip.open(audioStream);

            // 배경 음악을 무한 반복하도록 설정
            audioClip.loop(Clip.LOOP_CONTINUOUSLY);

            // 볼륨 컨트롤 초기화
            volumeControl = (FloatControl) audioClip.getControl(FloatControl.Type.MASTER_GAIN);

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            logger.severe("Bgm initialization failed. Exception: " + e.getMessage());
        }
    }

    // 배경 음악 재생
    public void play() {
        audioClip.start();
    }

    // 배경 음악 정지
    public void stop() {
        audioClip.stop();
    }

    // 배경 음악 다시 재생
    public void replay() {
        audioClip.setFramePosition(0);
        audioClip.start();
    }

    // 배경 음악 볼륨 조절
    public void setVolume(float volume) {
        float gain = (0.20f * volume) - 20.0f;
        volumeControl.setValue(gain);
    }
}
