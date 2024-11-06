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
import java.util.HashMap;
import java.util.Map;
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
    public ResponseEntity<Map<String, String>> index(@RequestParam("file")MultipartFile multipartFile) throws IOException {

        if (multipartFile.isEmpty()) {
            log.warn("File is empty");

            throw new BadRequestException("File is empty");
        }

        log.info("File is received {}", multipartFile.getOriginalFilename());

        String taskId = sendFile(multipartFile);

        Map<String, String> result = new HashMap<>();
        result.put("taskId", taskId);

        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @GetMapping("/getText{taskId}")
    public ResponseEntity<Map<String, String>> getText(@PathVariable("taskId") String taskId) {
        if (taskId == null) {
            log.warn("Task id is empty");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Map<String, String> resultResponse = new HashMap<>();

        Status redisTaskStatus = Status.fromString(redisService.getTaskStatus(taskId));

        if (redisTaskStatus == null) {
            resultResponse.put("description", "Not task id");
            return new ResponseEntity<>(resultResponse, HttpStatus.BAD_REQUEST);
        }

        log.info("Task id is {}", taskId);

        switch (redisTaskStatus) {
            case PROCESSING:
                resultResponse.put("description", "Task is processing");
                return new ResponseEntity<>(resultResponse, HttpStatus.OK);
            case ERROR:
                resultResponse.put("description", "Server is error");
                return new ResponseEntity<>(resultResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            case ERROR_FORMAT:
                resultResponse.put("description", "Task is error format file");
                return new ResponseEntity<>(resultResponse, HttpStatus.BAD_REQUEST);
            case DONE:
                String result = redisService.getTaskResult(taskId);
                resultResponse.put("description", "Task is done");
                resultResponse.put("taskIdResult", result);
                return new ResponseEntity<>(resultResponse, HttpStatus.OK);
            default:
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    private String sendFile(MultipartFile multipartFile) throws IOException {
        String taskId = UUID.randomUUID().toString();
        String fileName = multipartFile.getOriginalFilename();
        byte[] audioBytes = multipartFile.getBytes();
        String encodedFile = Base64.getEncoder().encodeToString(audioBytes);
        FileMessageDto fileMessageDto = new FileMessageDto(taskId, fileName, encodedFile);

        audioMessageService.sendAudioMessage(fileMessageDto);
        redisService.createNewTask(taskId);

        log.info("File is sent with task id {}", taskId);
        return taskId;
    }
}
