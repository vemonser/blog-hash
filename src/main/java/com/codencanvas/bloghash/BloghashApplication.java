package com.codencanvas.bloghash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class BloghashApplication {

	public static void main(String[] args) {
		SpringApplication.run(BloghashApplication.class, args);
	}

}
