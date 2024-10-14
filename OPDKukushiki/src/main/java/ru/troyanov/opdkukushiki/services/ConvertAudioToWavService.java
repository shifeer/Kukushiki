package ru.troyanov.opdkukushiki.services;

import lombok.extern.slf4j.Slf4j;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;

@Slf4j
public class ConvertAudioToWavService {

    private static boolean IS_WAV_FORMAT;

    public static File convertAudioToWav(File audioFile) throws IOException, UnsupportedAudioFileException, InterruptedException {

        if (!isAudioFormat(audioFile) && audioFile.exists() && audioFile.delete()) {
            throw new UnsupportedAudioFileException();
        }

        isWav16kHzMono(audioFile);

        if (IS_WAV_FORMAT) {
            return audioFile;
        }

        File resFile = new File(audioFile.getAbsolutePath() + ".wav");
        ProcessBuilder processBuilder = new ProcessBuilder("ffmpeg", "-i", audioFile.getAbsolutePath(), "-ar", "16000", "-ac", "1", resFile.getAbsolutePath());
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;

        while ((line = bufferedReader.readLine()) != null) {
            log.info(line);
        }

        int exitCode = process.waitFor();

        if (exitCode == 0) {
            log.info("{} converted successfully", audioFile.getAbsolutePath());
            if (audioFile.exists() && audioFile.delete()) {
                log.info("{} deleted successfully", audioFile.getAbsolutePath());
            }
        } else {
            log.error("{} convert failed", audioFile.getAbsolutePath());
        }

        return resFile;
    }

    private static void isWav16kHzMono(File audioFile) throws IOException {

        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
            AudioFormat audioFormat = audioInputStream.getFormat();

            if (audioFormat.getSampleRate() == 16000 && audioFormat.getChannels() == 1 && audioFormat.getEncoding() == AudioFormat.Encoding.PCM_SIGNED) {
                IS_WAV_FORMAT = true;
            }

        } catch (UnsupportedAudioFileException e) {
            IS_WAV_FORMAT = false;
        }
    }

    private static boolean isAudioFormat(File file) {

        String fileName = file.getName().toLowerCase();

        return fileName.endsWith(".wav") ||
                fileName.endsWith(".mp3") ||
                fileName.endsWith(".ogg");
    }
}
