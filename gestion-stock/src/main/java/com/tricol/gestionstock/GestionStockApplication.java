package com.tricol.gestionstock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootApplication
public class GestionStockApplication implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    public static void main(String[] args) {
        SpringApplication.run(GestionStockApplication.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("🔍 Testing database connection...");

        try (Connection connection = dataSource.getConnection()) {
            System.out.println("✅ Successfully connected to database: " + connection.getMetaData().getURL());
        } catch (Exception e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
        }
    }
}
