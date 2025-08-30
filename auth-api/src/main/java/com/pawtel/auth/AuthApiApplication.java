package com.pawtel.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuthApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthApiApplication.class, args);
	}

}


@Bean
org.springframework.web.client.RestTemplate restTemplate() {
	return new org.springframework.web.client.RestTemplate();
}

