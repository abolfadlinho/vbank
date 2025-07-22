package com.vbank.loggingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LoggingserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoggingserviceApplication.class, args);
		System.out.println("Kafka is listening to everything...");
	}

}
