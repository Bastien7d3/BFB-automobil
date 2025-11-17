package com.BFB.automobile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Application Spring Boot pour la gestion de locations automobiles BFB
 * @EnableScheduling : Active les tâches planifiées (traitement automatique des contrats)
 */
@SpringBootApplication
@EnableScheduling
public class AutomobileApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutomobileApplication.class, args);
	}

}
