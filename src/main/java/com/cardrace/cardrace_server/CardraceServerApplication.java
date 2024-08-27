package com.cardrace.cardrace_server;

import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CardraceServerApplication implements CommandLineRunner {

	@Autowired
	private SocketIOServer server;

	public static void main(String[] args) {
		SpringApplication.run(CardraceServerApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		server.start();
	}

}
