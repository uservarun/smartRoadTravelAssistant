package com.sih.roadassistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
@EnableAsync
@EnableScheduling // Enables the scheduled gate auto-reopen tasks
public class RoadassistantApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoadassistantApplication.class, args);
	}

}
