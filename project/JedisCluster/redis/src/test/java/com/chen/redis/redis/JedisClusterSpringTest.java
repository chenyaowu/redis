package com.chen.redis.redis;

import com.chen.redis.base.test.BaseTest;
import com.chen.redis.service.RedisService;
import org.junit.Test;

import javax.annotation.Resource;

public class JedisClusterSpringTest extends BaseTest {

    @Resource(name = "redisClusterService")
    private RedisService redisClusterService;

    @Test
    public void test(){
        assertNotNull(redisClusterService);
        redisClusterService.set("hello","world");
        assertTrue("world".equals(redisClusterService.get("hello")));
    }

}
