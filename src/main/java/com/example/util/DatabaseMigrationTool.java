package com.example.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DatabaseMigrationTool {

    private final String jdbcUrl;
    private final String username;
    private final String password;

    public DatabaseMigrationTool(String jdbcUrl, String username, String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    public void executeMigration() {
        System.out.println("Démarrage de la migration de la base de données...");

        // Charger le driver H2
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver H2 introuvable !");
            e.printStackTrace();
            return;
        }

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            System.out.println("Connexion réussie !");

            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream("migration_v2.sql");

            if (inputStream == null) {
                throw new RuntimeException("Script migration_v2.sql non trouvé !");
            }

            String migrationScript = new BufferedReader(new InputStreamReader(inputStream))
                    .lines().collect(Collectors.joining("\n"));

            String[] instructions = migrationScript.split(";");

            try (Statement statement = connection.createStatement()) {
                int count = 0;
                for (String instruction : instructions) {
                    String trimmed = instruction.trim();
                    // Ignorer les lignes vides et les commentaires
                    if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                        try {
                            statement.execute(trimmed);
                            count++;
                            System.out.println("OK: " + trimmed.substring(0,
                                    Math.min(60, trimmed.length())) + "...");
                        } catch (SQLException e) {
                            // Ignorer les erreurs "déjà existant"
                            System.out.println("Ignoré: " + e.getMessage());
                        }
                    }
                }
                System.out.println("\nMigration terminée ! "
                        + count + " instruction(s) exécutée(s).");
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de la migration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        DatabaseMigrationTool migrationTool = new DatabaseMigrationTool(
                "jdbc:h2:mem:lab8;DB_CLOSE_DELAY=-1",
                "root",
                ""
        );
        migrationTool.executeMigration();
    }
}