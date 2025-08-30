package com.livebmw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LiveBmwApplication {

	public static void main(String[] args) {
		SpringApplication.run(LiveBmwApplication.class, args);
	}

}
