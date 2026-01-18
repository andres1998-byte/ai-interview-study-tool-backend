package com.andres.ai_study_tool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AiStudyToolApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiStudyToolApplication.class, args);
	}

}
