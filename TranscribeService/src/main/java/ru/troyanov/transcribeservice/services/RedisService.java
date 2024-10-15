package ru.troyanov.transcribeservice.services;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ru.troyanov.Redis.Status;

@Service
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setResult(String taskId, String result) {
        redisTemplate.opsForHash().put(taskId, "status", Status.DONE.toString());
        redisTemplate.opsForHash().put(taskId, "result", result);
    }

    public void setStatusError(String taskId, Status status) {
        redisTemplate.opsForHash().put(taskId, "status", status);
    }
}
