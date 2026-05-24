package com.monserrat.monserrat_backend;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.monserrat")
@EntityScan("com.monserrat.entity")
@EnableJpaRepositories("com.monserrat.repository")
public class MonserratBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MonserratBackendApplication.class, args);
	}

}
