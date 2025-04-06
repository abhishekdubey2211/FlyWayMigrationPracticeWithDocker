package com.security;

import com.security.controller.UserController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class SecurityServiceApplication {
	private static final Logger log = LogManager.getLogger(SecurityServiceApplication.class);
	public static void main(String[] args) {
		SpringApplication.run(SecurityServiceApplication.class, args);
		log.info("***********FlyWayApiApplication started *************");
	}
}