package com.example.noticeservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class NoticeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NoticeServiceApplication.class, args);
    }

}
