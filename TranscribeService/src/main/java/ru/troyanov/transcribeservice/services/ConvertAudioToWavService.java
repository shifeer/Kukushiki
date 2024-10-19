package ru.troyanov.transcribeservice.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
@Slf4j
public class ConvertAudioToWavService {

    private static boolean IS_WAV_FORMAT;

    public static File convertAudioToWav(File audioFile) throws IOException, UnsupportedAudioFileException, InterruptedException {

        if (!isAudioFormat(audioFile)) {
            if (audioFile.exists() && audioFile.delete()) {
                log.info("Audio file deleted");
            } else {
                log.info("Audio file not deleted");
            }

            throw new UnsupportedAudioFileException("Unsupported audio file format");
        }

        isWav16kHzMono(audioFile);

        if (IS_WAV_FORMAT) {
            return audioFile;
        } else {
            return convert(audioFile);
        }
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
                fileName.endsWith(".ogg") ||
                fileName.endsWith(".wave");
    }

    private static File convert(File audioFile) throws IOException, InterruptedException {
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
            return resFile;
        } else {
            log.error("{} convert failed", audioFile.getAbsolutePath());
            if (audioFile.exists() && audioFile.delete()) {
                log.info("{} deleted successfully", audioFile.getAbsolutePath());
            } else {
                log.warn("Could not delete file {}", audioFile.getAbsolutePath());
            }

            return null;
        }
    }
}
