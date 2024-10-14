package ru.troyanov.transcribeservice.services;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setStatus(String taskId) {
        redisTemplate.opsForHash().put(taskId, "status", "done");
    }

    public void setResult(String taskId, String result) {
        redisTemplate.opsForHash().put(taskId, "result", result);
    }

    public String getStatus(String taskId) {
        return (String) redisTemplate.opsForHash().get(taskId, "status");
    }

    public void setStatusError(String taskId) {
        redisTemplate.opsForHash().put(taskId, "status", "error");
    }

    public void setStatusErrorUnsupportedFormat(String taskId) {
        redisTemplate.opsForHash().put(taskId, "status", "error_format");
    }
}
