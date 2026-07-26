package com.lifelink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableAsync
public class LifeLinkApplication {

	public static void main(String[] args) {
		SpringApplication.run(LifeLinkApplication.class, args);
	}

}

