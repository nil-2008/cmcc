package com.nil.web.config;

import com.nil.web.utils.RedisUtils;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * session存放redis以及redis工具初始化
 *
 * @author lianyou
 * @version 1.0
 * @date 2019/1/6 10:22
 */
@Configuration
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class RedisSessionConfig {

  /** 封装RedisTemplate */
  @Bean(name = "redisUtils")
  public RedisUtils redisUtil() {
    RedisUtils redisUtil = new RedisUtils();
    return redisUtil;
  }
}
