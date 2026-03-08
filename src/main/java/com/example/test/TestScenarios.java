package com.example.test;

import com.example.model.*;
import com.example.service.ReservationService;
import com.example.service.SalleService;
import com.example.util.PaginationResult;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.OptimisticLockException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class TestScenarios {

    private final EntityManagerFactory emf;
    private final SalleService salleService;
    private final ReservationService reservationService;

    public TestScenarios(EntityManagerFactory emf,
                         SalleService salleService,
                         ReservationService reservationService) {
        this.emf = emf;
        this.salleService = salleService;
        this.reservationService = reservationService;
    }

    public void runAllTests() {
        System.out.println("\n=== EXÉCUTION DES SCÉNARIOS DE TEST ===\n");
        testRechercheDisponibilite();
        testRechercheMultiCriteres();
        testPagination();
        testOptimisticLocking();
        testCachePerformance();
        System.out.println("\n=== TOUS LES TESTS TERMINÉS ===\n");
    }

    // ── TEST 1 : Disponibilité ──────────────────────────────────────────────
    private void testRechercheDisponibilite() {
        System.out.println("\n=== TEST 1: RECHERCHE DE DISPONIBILITÉ ===");

        LocalDateTime debut = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0);
        LocalDateTime fin   = debut.plusHours(3);

        System.out.println("Créneau testé : " + debut + " → " + fin);
        List<Salle> disponibles = salleService.findAvailableRooms(debut, fin);
        System.out.println("Salles disponibles : " + disponibles.size());

        disponibles.stream().limit(5).forEach(s ->
                System.out.println("  - " + s.getNom()
                        + " (capacité: " + s.getCapacite()
                        + ", bâtiment: " + s.getBatiment() + ")")
        );

        // Vérifier qu'une salle réservée est bien exclue
        EntityManager em = emf.createEntityManager();
        try {
            Reservation r = em.createQuery(
                            "SELECT r FROM Reservation r WHERE r.statut = :s", Reservation.class)
                    .setParameter("s", StatutReservation.CONFIRMEE)
                    .setMaxResults(1).getSingleResult();

            List<Salle> dispoSurCreneauReserve = salleService.findAvailableRooms(
                    r.getDateDebut(), r.getDateFin());

            System.out.println("Salle déjà réservée exclue des résultats ? "
                    + !dispoSurCreneauReserve.contains(r.getSalle()));
        } finally {
            em.close();
        }
    }

    // ── TEST 2 : Multi-critères ─────────────────────────────────────────────
    private void testRechercheMultiCriteres() {
        System.out.println("\n=== TEST 2: RECHERCHE MULTI-CRITÈRES ===");

        // Capacité >= 30 + équipement ID 1
        Map<String, Object> c1 = new HashMap<>();
        c1.put("capaciteMin", 30);
        c1.put("equipement", 1L);
        List<Salle> r1 = salleService.searchRooms(c1);
        System.out.println("Capacité >= 30 avec équipement #1 : " + r1.size() + " salle(s)");
        r1.forEach(s -> System.out.println("  - " + s.getNom() + " (" + s.getCapacite() + " places)"));

        // Bâtiment C, étage 2
        Map<String, Object> c2 = new HashMap<>();
        c2.put("batiment", "Bâtiment C");
        c2.put("etage", 2);
        List<Salle> r2 = salleService.searchRooms(c2);
        System.out.println("\nBâtiment C, étage 2 : " + r2.size() + " salle(s)");
        r2.forEach(s -> System.out.println("  - " + s.getNom()));

        // Combiné : capacité 20-50, Bâtiment B, ordinateur fixe
        Map<String, Object> c3 = new HashMap<>();
        c3.put("capaciteMin", 20);
        c3.put("capaciteMax", 50);
        c3.put("batiment", "Bâtiment B");
        c3.put("equipement", 6L);
        List<Salle> r3 = salleService.searchRooms(c3);
        System.out.println("\nCombinée (20-50 places, Bât. B, PC) : " + r3.size() + " salle(s)");
        r3.forEach(s -> System.out.println("  - " + s.getNom() + " (" + s.getCapacite() + " places)"));
    }

    // ── TEST 3 : Pagination ─────────────────────────────────────────────────
    private void testPagination() {
        System.out.println("\n=== TEST 3: PAGINATION ===");

        int pageSize = 5;
        int totalPages = salleService.getTotalPages(pageSize);
        System.out.println("Total pages (taille " + pageSize + ") : " + totalPages);

        for (int page = 1; page <= totalPages; page++) {
            List<Salle> salles = salleService.getPaginatedRooms(page, pageSize);
            System.out.println("Page " + page + " :");
            salles.forEach(s -> System.out.println("  - " + s.getNom()
                    + " (" + s.getBatiment() + ")"));
        }

        // PaginationResult
        long total = salleService.countRooms();
        List<Salle> firstPage = salleService.getPaginatedRooms(1, pageSize);
        PaginationResult<Salle> pr = new PaginationResult<>(firstPage, 1, pageSize, total);

        System.out.println("\nPaginationResult :");
        System.out.println("  Page courante  : " + pr.getCurrentPage());
        System.out.println("  Total pages    : " + pr.getTotalPages());
        System.out.println("  Total éléments : " + pr.getTotalItems());
        System.out.println("  Page suivante  : " + pr.hasNext());
        System.out.println("  Page précédente: " + pr.hasPrevious());
    }

    // ── TEST 4 : Optimistic Locking ─────────────────────────────────────────
    private void testOptimisticLocking() {
        System.out.println("\n=== TEST 4: OPTIMISTIC LOCKING ===");

        EntityManager em = emf.createEntityManager();
        Reservation reservation;
        try {
            reservation = em.createQuery(
                            "SELECT r FROM Reservation r WHERE r.statut = :s", Reservation.class)
                    .setParameter("s", StatutReservation.CONFIRMEE)
                    .setMaxResults(1).getSingleResult();
            System.out.println("Réservation ID=" + reservation.getId()
                    + ", salle=" + reservation.getSalle().getNom());
        } finally {
            em.close();
        }

        final Long id = reservation.getId();
        CountDownLatch latch = new CountDownLatch(1);
        ExecutorService exec = Executors.newFixedThreadPool(2);

        // Thread 1 : modifie le motif (avec délai pour laisser Thread 2 lire d'abord)
        exec.submit(() -> {
            try {
                latch.await();
                EntityManager em1 = emf.createEntityManager();
                try {
                    em1.getTransaction().begin();
                    Reservation r1 = em1.find(Reservation.class, id);
                    System.out.println("Thread 1 lu, version=" + r1.getVersion());
                    Thread.sleep(1000); // laisse Thread 2 lire la même version
                    r1.setMotif("Motif modifié par Thread 1");
                    em1.merge(r1);
                    em1.getTransaction().commit();
                    System.out.println("Thread 1 : commit réussi !");
                } catch (OptimisticLockException e) {
                    System.out.println("Thread 1 : conflit détecté → rollback");
                    if (em1.getTransaction().isActive()) em1.getTransaction().rollback();
                } finally {
                    em1.close();
                }
            } catch (Exception e) { e.printStackTrace(); }
        });

        // Thread 2 : modifie la date de fin
        exec.submit(() -> {
            try {
                latch.await();
                Thread.sleep(100); // lit après Thread 1 mais commit avant
                EntityManager em2 = emf.createEntityManager();
                try {
                    em2.getTransaction().begin();
                    Reservation r2 = em2.find(Reservation.class, id);
                    System.out.println("Thread 2 lu, version=" + r2.getVersion());
                    r2.setDateFin(r2.getDateFin().plusHours(1));
                    em2.merge(r2);
                    em2.getTransaction().commit();
                    System.out.println("Thread 2 : commit réussi !");
                } catch (OptimisticLockException e) {
                    System.out.println("Thread 2 : conflit détecté → rollback");
                    if (em2.getTransaction().isActive()) em2.getTransaction().rollback();
                } finally {
                    em2.close();
                }
            } catch (Exception e) { e.printStackTrace(); }
        });

        latch.countDown();
        exec.shutdown();
        try { exec.awaitTermination(10, TimeUnit.SECONDS); }
        catch (InterruptedException e) { e.printStackTrace(); }

        // État final
        EntityManager emFinal = emf.createEntityManager();
        try {
            Reservation final_ = emFinal.find(Reservation.class, id);
            System.out.println("\nÉtat final : motif='" + final_.getMotif()
                    + "', dateFin=" + final_.getDateFin()
                    + ", version=" + final_.getVersion());
        } finally {
            emFinal.close();
        }
    }

    // ── TEST 5 : Performance cache ──────────────────────────────────────────
    private void testCachePerformance() {
        System.out.println("\n=== TEST 5: PERFORMANCE DU CACHE ===");

        // Sans cache (cache vidé)
        emf.getCache().evictAll();
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            EntityManager em = emf.createEntityManager();
            try { em.find(Salle.class, (i % 15) + 1L).getEquipements().size(); }
            finally { em.close(); }
        }
        System.out.println("Sans cache : " + (System.currentTimeMillis() - t1) + " ms");

        // Avec cache (même données, cache chaud)
        long t2 = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            EntityManager em = emf.createEntityManager();
            try { em.find(Salle.class, (i % 15) + 1L).getEquipements().size(); }
            finally { em.close(); }
        }
        System.out.println("Avec cache : " + (System.currentTimeMillis() - t2) + " ms");

        // Requêtes avec et sans query cache
        emf.getCache().evictAll();
        long t3 = System.currentTimeMillis();
        for (int i = 0; i < 20; i++) {
            EntityManager em = emf.createEntityManager();
            try {
                em.createQuery("SELECT s FROM Salle s WHERE s.capacite >= :c", Salle.class)
                        .setParameter("c", 30).getResultList();
            } finally { em.close(); }
        }
        System.out.println("Requêtes sans query cache : " + (System.currentTimeMillis() - t3) + " ms");

        long t4 = System.currentTimeMillis();
        for (int i = 0; i < 20; i++) {
            EntityManager em = emf.createEntityManager();
            try {
                em.createQuery("SELECT s FROM Salle s WHERE s.capacite >= :c", Salle.class)
                        .setParameter("c", 30)
                        .setHint("org.hibernate.cacheable", "true")
                        .getResultList();
            } finally { em.close(); }
        }
        System.out.println("Requêtes avec query cache : " + (System.currentTimeMillis() - t4) + " ms");
    }
}