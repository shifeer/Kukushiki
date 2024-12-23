package ru.troyanov.transcribeservice.services;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Service
@Slf4j
@Setter
public class TranscriptionService {

    @Value("${vosk.path-model}")
    private String PATH_MODEL;

    public String rec(File file) throws IOException, UnsupportedAudioFileException {

        try (Model model = new Model(PATH_MODEL);
             AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(file)));
             Recognizer recognizer = new Recognizer(model, 16000)) {

            StringBuilder sb = new StringBuilder();
            int nbytes;
            byte[] b = new byte[4096];

            while ((nbytes = ais.read(b)) > 0) {
                if (recognizer.acceptWaveForm(b, nbytes)) {
                    sb.append(extractTextFromJson(recognizer.getResult())).append(" ");
                }
            }

            sb.append(extractTextFromJson(recognizer.getFinalResult()));

            log.info("{} is transcribed", file.getAbsolutePath());

            return sb.toString();
        } finally {
            if (file.exists() && file.delete()) {
                log.info("{} is deleted", file.getAbsolutePath());
            } else {
                log.warn("Not deleted is {}", file.getAbsolutePath());
            }
        }
    }

    private String extractTextFromJson(String jsonString) {
        JSONObject jsonObject = new JSONObject(jsonString);
        return jsonObject.optString("text");
    }
}
