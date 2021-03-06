package com.atguigu.srb.mail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.atguigu.srb", "com.atguigu.common"})
@EnableFeignClients
public class ServiceMailApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceMailApplication.class, args);
    }
}
