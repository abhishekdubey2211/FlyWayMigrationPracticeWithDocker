package com.security;

import com.security.config.redis.RedisConfiguration;
import com.security.config.redis.RedisMethods;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class SecurityServiceApplication {

	private static final Logger log = LogManager.getLogger(SecurityServiceApplication.class);

	private RedisConfiguration rs = new RedisConfiguration();

	@Autowired
	private RedisMethods cm;

	public static void main(String[] args) {
		SpringApplication.run(SecurityServiceApplication.class, args);
		log.info("*********** SecurityServiceApplication started *************");
	}

	@PostConstruct
	public void postConstruct() {
		rs.initializeRedis();

		// Setting and retrieving data
		cm.setRedisData("ABHISHEK", "HELLO WORLD");
		log.info("Stored Data: " + cm.getRedisData("ABHISHEK"));

		// Setting hash data
		cm.setMemoryValue("User:1001", "Name", "Abhishek");
		cm.setMemoryValue("User:1001", "Email", "abdubey42@gmail.com");

		// Retrieving hash data
		log.info("User Data: " + cm.getRedisHData("User:1001"));

		// List operations
		cm.rpush("UserList", "", "");  // Add empty elements to create list of length 2
		cm.lset("UserList", 0, "User1");
		cm.lset("UserList", 1, "User2");

		log.info("List Element at Index 0: " + cm.lindex("UserList", 0));

		// Start subscriber in a separate thread
		new Thread(() -> cm.subscribe("COUNT")).start();

		// Publish messages
		for (int i = 0; i < 50; i++) {
			cm.publish("COUNT", "Message  Hello Mr. Abhishek" + i);
		}

		// Deleting data
		cm.delRedisData("ABHISHEK");
		log.info("Data after deletion: " + cm.getRedisData("ABHISHEK"));

		log.info("All channels: " + cm.getAllChannels());
	}
}
