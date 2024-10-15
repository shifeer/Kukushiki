package ru.troyanov.opdkukushiki.controllers;

import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.troyanov.FileMessageDto;
import ru.troyanov.Redis.Status;
import ru.troyanov.opdkukushiki.services.AudioMessageService;
import ru.troyanov.opdkukushiki.services.RedisService;

import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@RestController()
@Slf4j
@RequestMapping("/api/v1/transcription")
public class RestTranscriptionController {

    private final AudioMessageService audioMessageService;
    private final RedisService redisService;

    public RestTranscriptionController(AudioMessageService audioMessageService, RedisService redisService) {
        this.audioMessageService = audioMessageService;
        this.redisService = redisService;
    }

    @PostMapping("/new")
    public ResponseEntity<String> index(@RequestParam("file")MultipartFile multipartFile) throws IOException {

        if (multipartFile.isEmpty()) {
            log.warn("File is empty");

            throw new BadRequestException("File is empty");
        }

        log.info("File is received {}", multipartFile.getOriginalFilename());

        sendFile(multipartFile);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/getText{taskId}")
    public ResponseEntity<String> getText(@PathVariable("taskId") String taskId) {
        if (taskId == null || taskId.isEmpty()) {
            log.warn("Task id is empty");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Status redisTaskStatus = Status.fromString(redisService.getTaskStatus(taskId));

        log.info("Task id is {}", taskId);

        switch (redisTaskStatus) {
            case PROCESSING:
                return new ResponseEntity<>("Task is processing", HttpStatus.NO_CONTENT);
            case ERROR:
                return new ResponseEntity<>("Error service", HttpStatus.INTERNAL_SERVER_ERROR);
            case ERROR_FORMAT:
                return new ResponseEntity<>("Error formatting", HttpStatus.BAD_REQUEST);
            case DONE:
                String result = redisService.getTaskResult(taskId);
                return new ResponseEntity<>(result, HttpStatus.OK);
            default:
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    private void sendFile(MultipartFile multipartFile) throws IOException {
        String taskId = UUID.randomUUID().toString();
        String fileName = multipartFile.getOriginalFilename();
        byte[] audioBytes = multipartFile.getBytes();
        String encodedFile = Base64.getEncoder().encodeToString(audioBytes);
        FileMessageDto fileMessageDto = new FileMessageDto(taskId, fileName, encodedFile);

        audioMessageService.sendAudioMessage(fileMessageDto);
        redisService.createNewTask(taskId);

        log.info("File is sent with task id {}", taskId);
    }
}
