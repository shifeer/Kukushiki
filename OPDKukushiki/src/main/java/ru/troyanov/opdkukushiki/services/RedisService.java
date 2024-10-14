package ru.troyanov.opdkukushiki.services;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void createNewTask(String taskId) {
        redisTemplate.opsForHash().put(taskId, "status", "processing");
        redisTemplate.opsForHash().put(taskId, "result", "");
        redisTemplate.expire(taskId, Duration.ofHours(3));
    }

    public String getTaskStatus(String taskId) {
        return (String) redisTemplate.opsForHash().get(taskId, "status");
    }

    public String getTaskResult(String taskId) {
        return (String) redisTemplate.opsForHash().get(taskId, "result");
    }
}
