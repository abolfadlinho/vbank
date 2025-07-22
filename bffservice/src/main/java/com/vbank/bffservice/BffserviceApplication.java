package com.vbank.bffservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class BffserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BffserviceApplication.class, args);
        System.out.println("vBank up and running...");
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
