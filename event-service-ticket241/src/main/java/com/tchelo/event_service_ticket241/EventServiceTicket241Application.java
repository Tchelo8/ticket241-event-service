package com.tchelo.event_service_ticket241;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//plus besoin de mettre le @EnableEurekaClient comme dans les anc vrs ici car le .yml le rends déjà en compte
public class EventServiceTicket241Application {

	public static void main(String[] args) {
		SpringApplication.run(EventServiceTicket241Application.class, args);
	}

}
