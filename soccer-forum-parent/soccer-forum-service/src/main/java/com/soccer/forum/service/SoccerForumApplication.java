package com.soccer.forum.service;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.soccer.forum"})
@MapperScan("com.soccer.forum.service.mapper")
public class SoccerForumApplication {
    public static void main(String[] args) {
        SpringApplication.run(SoccerForumApplication.class, args);
    }
}
