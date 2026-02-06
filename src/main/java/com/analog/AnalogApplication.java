package com.analog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AnalogApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnalogApplication.class, args);
	}

}
