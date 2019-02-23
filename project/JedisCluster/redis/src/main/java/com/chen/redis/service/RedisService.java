package com.chen.redis.service;


public interface RedisService {
    String set(String key, String value);

    String get(String key);

}
