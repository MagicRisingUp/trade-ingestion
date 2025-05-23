package com.magiccode.tradeingestion.config;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.net.Socket;
import java.io.IOException;

@Component
@Profile("dev")
public class DevStartupChecker {

    @PostConstruct
    public void checkSolace() {
        try (Socket socket = new Socket("localhost", 55556)) {
            socket.close();
            System.out.println("Solace is reachable on port 55556");
        } catch (IOException e) {
            System.err.println("Error: Solace is not reachable on port 55556");
            System.err.println("Please ensure Solace is running before starting the application");
            System.exit(1);
        }
    }
} 