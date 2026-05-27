package com.paradoxdevs.dollar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.Socket;

@SpringBootApplication
public class DollarApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(DollarApplication.class);

		if (isPostgresAvailable()) {
			System.out.println(">>> PostgreSQL detected on 54322. Using Postgres profile.");
			app.setAdditionalProfiles("postgres");
		} else {
			System.out.println(">>> PostgreSQL NOT detected. Falling back to H2 profile.");
			app.setAdditionalProfiles("h2");
		}

		app.run(args);
	}

	private static boolean isPostgresAvailable() {
		try (Socket ignored = new Socket("localhost", 54322)) {
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
