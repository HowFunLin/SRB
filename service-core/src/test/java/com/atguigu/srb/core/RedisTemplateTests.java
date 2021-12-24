package com.atguigu.srb.core;

import com.atguigu.srb.core.mapper.DictMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisTemplateTests {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private DictMapper dictMapper;

    /**
     * 测试 Redis
     */
    @Test
    public void saveDict() {
        redisTemplate.opsForValue().set("dict", dictMapper.selectById(1), 1, TimeUnit.MINUTES);
    }

    @Test
    public void getDict() {
        System.out.println(redisTemplate.opsForValue().get("dict"));
    }
}
