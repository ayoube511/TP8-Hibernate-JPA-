# Lab 8 — Application de Réservation de Salles

## Description
Application Java de gestion de réservation de salles développée avec JPA/Hibernate,
EhCache pour le cache de second niveau et H2 comme base de données en mémoire.

## Technologies
- Java 11+
- JPA 2.2 / Hibernate 5
- EhCache 2
- H2 Database
- Maven

## Structure du projet
```
lab8/
├── src/main/java/com/example/
│   ├── model/
│   │   ├── Utilisateur.java
│   │   ├── Salle.java
│   │   ├── Reservation.java
│   │   ├── Equipement.java
│   │   └── StatutReservation.java
│   ├── repository/
│   │   ├── SalleRepository.java
│   │   ├── SalleRepositoryImpl.java
│   │   ├── ReservationRepository.java
│   │   └── ReservationRepositoryImpl.java
│   ├── service/
│   │   ├── SalleService.java
│   │   ├── SalleServiceImpl.java
│   │   ├── ReservationService.java
│   │   └── ReservationServiceImpl.java
│   ├── util/
│   │   ├── DataInitializer.java
│   │   ├── DatabaseMigrationTool.java
│   │   ├── PerformanceReport.java
│   │   └── PaginationResult.java
│   ├── test/
│   │   └── TestScenarios.java
│   └── App.java
├── src/main/resources/
│   ├── META-INF/
│   │   └── persistence.xml
│   ├── ehcache.xml
│   └── migration_v2.sql
└── pom.xml
```

## Prérequis
- Java 11 ou supérieur
- Maven 3.6+
- IntelliJ IDEA (recommandé)


## Utilisation

Au lancement, un menu interactif s'affiche :
```
=== MENU PRINCIPAL ===
1. Initialiser les données de test
2. Exécuter les scénarios de test
3. Exécuter le script de migration
4. Générer un rapport de performance
5. Quitter
```

### Option 1 — Initialiser les données
Génère automatiquement :
- 10 équipements (projecteur, écran, WiFi, etc.)
- 20 utilisateurs dans 10 départements
- 15 salles dans 3 bâtiments (A, B, C)
- 100 réservations aléatoires sur 90 jours

### Option 2 — Scénarios de test
Lance 5 tests automatiques :

| Test | Description                                                 |
|------|-------------------------------------------------------------|
| Test 1 | Recherche de salles disponibles sur un créneau            |
| Test 2 | Recherche multi-critères (capacité, bâtiment, équipement) |
| Test 3 | Pagination des résultats                                  |
| Test 4 | Optimistic Locking (gestion de la concurrence)            |
| Test 5 | Performance du cache de second niveau                     |

### Option 3 — Migration de base de données
Exécute le script `migration_v2.sql` qui :
- Sauvegarde les données existantes
- Ajoute de nouvelles colonnes
- Crée des index de performance
- Ajoute des contraintes
- Crée une vue et une procédure stockée

### Option 4 — Rapport de performance
Génère un fichier `performance_report_YYYYMMDD_HHMMSS.txt` dans :
```
C:\Users\lucif\IdeaProjects\lab8\
```
Le rapport contient :
- Temps d'exécution de chaque requête
- Nombre de requêtes SQL exécutées
- Statistiques du cache (hits/miss/ratio)
- Recommandations d'optimisation

## Modèle de données

### Entités et relations
```
Utilisateur (1) ──────── (N) Reservation (N) ──────── (1) Salle
                                                            |
                                                           (N)
                                                            |
                                                       Equipement
```

### Description des entités

**Utilisateur**
- id, nom, prenom, email, telephone, departement
- Une liste de réservations

**Salle**
- id, nom, capacite, description, batiment, etage, numero
- Une liste de réservations
- Un ensemble d'équipements

**Reservation**
- id, dateDebut, dateFin, motif, statut (CONFIRMEE / ANNULEE / EN_ATTENTE)
- Lié à un utilisateur et une salle

**Equipement**
- id, nom, description, reference
- Lié à plusieurs salles

## Configuration du cache EhCache

| Entité      | Max éléments | TTL  |
|-------------|--------------|------|
| Utilisateur | 1000         | 600s |
| Salle       | 500          | 600s |
| Reservation | 5000         | 600s |
| Equipement  | 100          | 600s |

## Exemple de rapport de performance
```
=== RAPPORT DE PERFORMANCE ===
Date: 2026-03-08 12:30:00
=================================

Test: Recherche de salles disponibles
  Temps d'exécution : 45ms
  Requêtes SQL      : 1
  Cache hits        : 0
  Cache miss        : 12
  Ratio cache       : 0.00%

Test: Accès répété avec cache
  Temps d'exécution : 320ms
  Requêtes SQL      : 1
  Cache hits        : 99
  Cache miss        : 1
  Ratio cache       : 99.00%

=== RECOMMANDATIONS ===
1. Optimisation du cache recommandée
2. Utiliser JOIN FETCH pour éviter le problème N+1
3. Surveiller en production avec JProfiler
```

## Concepts clés implémentés

### Optimistic Locking
Chaque entité possède un champ `@Version` qui détecte
les modifications concurrentes et lève une
`OptimisticLockException` en cas de conflit.
```java
@Version
private Long version;
```

### Cache de second niveau
Les entités annotées `@Cacheable` sont mises en cache
par EhCache pour éviter des requêtes SQL répétitives.
```java
@Entity
@Cacheable
public class Salle { ... }
```

### Pagination
```java
List<Salle> page = salleRepository.findPaginated(1, 5);
// Retourne les 5 premières salles
```

### Recherche multi-critères dynamique
```java
Map<String, Object> criteres = new HashMap<>();
criteres.put("capaciteMin", 30);
criteres.put("batiment", "Bâtiment B");
criteres.put("equipement", 1L);
List<Salle> salles = salleService.searchRooms(criteres);
```

## Auteur
**Mokhtar Ben Laghlagh**  
Lab 8 — JPA / Hibernate / EhCache

# video démonstrative :

https://github.com/user-attachments/assets/a031a06c-826b-436f-8caa-cb7ada855f76

