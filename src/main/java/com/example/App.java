package com.example;

import com.example.repository.*;
import com.example.service.*;
import com.example.test.TestScenarios;
import com.example.util.*;

import javax.persistence.*;
import java.util.Scanner;

public class App {

    public static void main(String[] args) {
        System.out.println("=== APPLICATION DE RÉSERVATION DE SALLES ===");

        EntityManagerFactory emf =
                Persistence.createEntityManagerFactory("gestion-reservations");
        EntityManager em = emf.createEntityManager();

        try {
            SalleRepository salleRepo         = new SalleRepositoryImpl(em);
            SalleService salleService         = new SalleServiceImpl(em, salleRepo);
            ReservationRepository resRepo     = new ReservationRepositoryImpl(em);
            ReservationService resService     = new ReservationServiceImpl(em, resRepo);

            Scanner scanner = new Scanner(System.in);
            boolean exit = false;

            while (!exit) {
                System.out.println("\n=== MENU PRINCIPAL ===");
                System.out.println("1. Initialiser les données de test");
                System.out.println("2. Exécuter les scénarios de test");
                System.out.println("3. Exécuter le script de migration");
                System.out.println("4. Générer un rapport de performance");
                System.out.println("5. Quitter");
                System.out.print("Votre choix : ");

                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        new DataInitializer(emf).initializeData();
                        break;
                    case 2:
                        new TestScenarios(emf, salleService, resService).runAllTests();
                        break;
                    case 3:
                        new DatabaseMigrationTool(
                                "jdbc:h2:mem:lab8;DB_CLOSE_DELAY=-1",
                                "sa",
                                ""
                        ).executeMigration();
                        break;
                    case 4:
                        new PerformanceReport(emf).runPerformanceTests();
                        break;
                    case 5:
                        exit = true;
                        System.out.println("Au revoir !");
                        break;
                    default:
                        System.out.println("Choix invalide.");
                }
            }
        } finally {
            em.close();
            emf.close();
        }
    }
}