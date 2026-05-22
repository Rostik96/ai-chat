package dev.rost.aichat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
class App {

	static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}
}
