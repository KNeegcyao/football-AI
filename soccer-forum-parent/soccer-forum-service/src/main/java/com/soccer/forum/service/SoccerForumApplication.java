package com.soccer.forum.service;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.soccer.forum.service.mapper")
public class SoccerForumApplication {
    public static void main(String[] args) {
        System.err.println("DEBUG: Application Starting...");
        SpringApplication.run(SoccerForumApplication.class, args);
        System.err.println("DEBUG: Application Started...");
    }
}
