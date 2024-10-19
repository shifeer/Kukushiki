package ru.troyanov.transcribeservice.controllers;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.troyanov.FileMessageDto;
import ru.troyanov.Redis.Status;
import ru.troyanov.transcribeservice.exceptions.DecodingException;
import ru.troyanov.transcribeservice.services.ConvertAudioToWavService;
import ru.troyanov.transcribeservice.services.CorrectTextService;
import ru.troyanov.transcribeservice.services.RedisService;
import ru.troyanov.transcribeservice.services.TranscriptionService;

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

        log.info(MessageFormat.format("Received file : {0} and task ID {1}", fileMessageDto.getFileName(), fileMessageDto.getTaskId()));

        String taskId = fileMessageDto.getTaskId();
        try {
            File file = decodeFile(taskId, fileMessageDto.getFileName(), fileMessageDto.getFileContent());
            File fileConverted = ConvertAudioToWavService.convertAudioToWav(file);
            String resultTranscribe = transcriptionService.rec(fileConverted);

            redisService.setResult(taskId, resultTranscribe);
        } catch (DecodingException e) {
            log.error(e.getMessage());
            redisService.setStatusError(taskId, Status.ERROR);
        } catch (IOException | InterruptedException e) {
            log.error("Error while working with file or stream");
            redisService.setStatusError(taskId, Status.ERROR);
        } catch (UnsupportedAudioFileException e) {
            log.warn("Audio file not supported");
            redisService.setStatusError(taskId, Status.ERROR_FORMAT);
        }

    }

    private File decodeFile(String taskId, String fileName, String fileContent) throws DecodingException {
        byte[] byteFileContent = Base64.getDecoder().decode(fileContent);
        File file = new File(MessageFormat.format("{0}/+{1}+{2}", filePath, taskId, fileName));
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(byteFileContent);
        } catch (IOException e) {
            log.error("Error decoding file", e);
            redisService.setStatusError(taskId, Status.ERROR);
            if (file.exists() && file.delete()) {
                log.info("Deleted file : {}", file.getAbsolutePath());
            } else {
                log.warn("Could not delete file: {}", file.getAbsolutePath());
            }
            throw new DecodingException("Error decoding file");
        }
        return file;
    }
}
