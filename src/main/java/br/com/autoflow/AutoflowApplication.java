package br.com.autoflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AutoflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutoflowApplication.class, args);
	}

}
