package com.sira;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		cargarDotenv();
		SpringApplication.run(BackendApplication.class, args);
	}

	private static void cargarDotenv() {
		try {
			Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
			dotenv.entries().forEach(e -> {
				if (System.getProperty(e.getKey()) == null) {
					System.setProperty(e.getKey(), e.getValue());
				}
			});
		} catch (Exception ignored) {}
	}
}
