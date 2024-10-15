package ru.troyanov.transcribeservice.controllers;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.troyanov.Redis.Status;
import ru.troyanov.transcribeservice.exceptions.DecodingException;
import ru.troyanov.transcribeservice.services.ConvertAudioToWavService;
import ru.troyanov.transcribeservice.services.CorrectTextService;
import ru.troyanov.transcribeservice.services.RedisService;
import ru.troyanov.transcribeservice.services.TranscriptionService;
import ru.troyanov.FileMessageDto;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Base64;

@Component
@Slf4j
@EnableRabbit
@Setter
public class RabbitFileReceiver {

    @Value("${path.audio-files:#{systemProperties['user.dir'] + '/TranscribeService/audioFiles'}}")
    private String filePath;
    private final TranscriptionService transcriptionService;
    private final RedisService redisService;
    private final CorrectTextService correctTextService;

    public RabbitFileReceiver(TranscriptionService transcriptionService, RedisService redisService, CorrectTextService correctTextService) {
        this.transcriptionService = transcriptionService;
        this.redisService = redisService;
        this.correctTextService = correctTextService;
    }

    @RabbitListener(queues = {"${rabbit.queue.nameForTranscribe}"})
    public void receive(FileMessageDto fileMessageDto) {
        log.info("Received file: {}", fileMessageDto.getFileName());

        String taskId = fileMessageDto.getTaskId();
        File file = decodeFile(taskId, fileMessageDto.getFileName(), fileMessageDto.getFileContent());
        File fileOptional;
        try {
            fileOptional = ConvertAudioToWavService.convertAudioToWav(file);
            if (fileOptional == null) {
                redisService.setStatusError(taskId, Status.ERROR);
                return;
            }
        } catch (InterruptedException | IOException e) {
            log.error("Error while converting audio to wav file", e);
            redisService.setStatusError(taskId, Status.ERROR);
            return;
        } catch (UnsupportedAudioFileException e) {
            log.error("Unsupported audio file", e);
            redisService.setStatusError(taskId, Status.ERROR_FORMAT);
            return;
        }

        String s = null;

        try {
            s = transcriptionService.rec(fileOptional);
        } catch (IOException e) {
            log.error("Error while transcribing file", e);
            redisService.setStatusError(taskId, Status.ERROR);
            return;
        } catch (UnsupportedAudioFileException e) {
            log.error("Unsupported audio file", e);
            redisService.setStatusError(taskId, Status.ERROR_FORMAT);
            return;
        }

        redisService.setResult(taskId, s);
    }

    private File decodeFile(String taskId, String fileName, String fileContent) {
        byte[] byteFileContent = Base64.getDecoder().decode(fileContent);
        File file = new File(MessageFormat.format("{0}/+{1}+{2}", filePath, taskId, fileName));
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(byteFileContent);
        } catch (IOException e) {
            log.error("Error decoding file", e);
            redisService.setStatusError(taskId, Status.ERROR);
        }
        return file;
    }
}
