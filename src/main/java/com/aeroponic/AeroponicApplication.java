package com.aeroponic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AeroponicApplication {
    public static void main(String[] args) {
        SpringApplication.run(AeroponicApplication.class, args);
        System.out.println("==================================================================");
        System.out.println(" 🌱 Aeroponic Farm Management & Nutrient Injection System Started");
        System.out.println(" 🌐 Web Dashboard: http://localhost:8080/");
        System.out.println(" 📡 REST API Base: http://localhost:8080/api/aeroponics");
        System.out.println("==================================================================");
    }
}
