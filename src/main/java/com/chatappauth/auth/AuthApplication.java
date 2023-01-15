package com.chatappauth.auth;

import it.ozimov.springboot.mail.configuration.EnableEmailTools;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@SpringBootApplication
@EntityScan("com.chatcomponents")
@EnableEmailTools
@EnableJpaRepositories(basePackages = "com.chatappauth.auth.repository")
@EnableRedisRepositories(basePackages = "com.chatappauth.auth.blacklist")
public class AuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthApplication.class, args);
	}
}
