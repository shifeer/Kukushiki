package ru.troyanov.opdkukushiki.services;

import ru.troyanov.FileMessageDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AudioMessageService {

    @Value("${rabbit.queue.nameForTranscribe}")
    private String queueName;

    private final RabbitTemplate rabbitTemplate;

    public AudioMessageService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendAudioMessage(FileMessageDto fileMessageDto) {
        rabbitTemplate.convertAndSend(queueName, fileMessageDto);
    }
}
