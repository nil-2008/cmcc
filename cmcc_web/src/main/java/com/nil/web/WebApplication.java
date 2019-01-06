package com.nil.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;

/**
 * @SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
 *
 * @author lianyou
 * @version 1.0
 * @date 2019/1/4 21:38
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableCaching
public class WebApplication {

  public static void main(String[] args) {
    SpringApplication.run(WebApplication.class, args);
  }
}
