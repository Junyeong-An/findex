package com.sprint.mission.findex;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableCaching
@EnableJpaAuditing
@SpringBootApplication
public class FindexApplication {

	public static void main(String[] args) {

		SpringApplication.run(FindexApplication.class, args);
	}
}
