-- Script de migration v2.0

-- 1. Sauvegarde
CREATE TABLE IF NOT EXISTS backup_utilisateurs AS SELECT * FROM utilisateurs;
CREATE TABLE IF NOT EXISTS backup_salles AS SELECT * FROM salles;
CREATE TABLE IF NOT EXISTS backup_reservations AS SELECT * FROM reservations;
CREATE TABLE IF NOT EXISTS backup_equipements AS SELECT * FROM equipements;
CREATE TABLE IF NOT EXISTS backup_salle_equipement AS SELECT * FROM salle_equipement;

-- 2. Nouvelles colonnes
ALTER TABLE utilisateurs ADD COLUMN IF NOT EXISTS departement VARCHAR(100);
ALTER TABLE salles ADD COLUMN IF NOT EXISTS numero VARCHAR(20);
ALTER TABLE salles ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE equipements ADD COLUMN IF NOT EXISTS reference VARCHAR(50);
ALTER TABLE equipements ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS statut VARCHAR(20) DEFAULT 'CONFIRMEE';
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

-- 3. Mise à jour données
UPDATE reservations SET statut = 'CONFIRMEE' WHERE statut IS NULL;

-- 4. Index de performance
CREATE INDEX IF NOT EXISTS idx_reservation_dates ON reservations(date_debut, date_fin);
CREATE INDEX IF NOT EXISTS idx_reservation_statut ON reservations(statut);
CREATE INDEX IF NOT EXISTS idx_salle_capacite ON salles(capacite);
CREATE INDEX IF NOT EXISTS idx_salle_batiment_etage ON salles(batiment, etage);

-- 5. Contraintes
ALTER TABLE reservations ADD CONSTRAINT IF NOT EXISTS check_dates_coherentes
    CHECK (date_fin > date_debut);
ALTER TABLE reservations ADD CONSTRAINT IF NOT EXISTS check_statut_valide
    CHECK (statut IN ('CONFIRMEE', 'ANNULEE', 'EN_ATTENTE'));

-- 6. Vue rapports
CREATE OR REPLACE VIEW vue_reservations_completes AS
SELECT r.id, r.date_debut, r.date_fin, r.motif, r.statut,
       u.nom AS nom_utilisateur, u.prenom, u.email,
       s.nom AS nom_salle, s.capacite, s.batiment, s.etage, s.numero
FROM reservations r
         JOIN utilisateurs u ON r.utilisateur_id = u.id
         JOIN salles s ON r.salle_id = s.id;

-- 7. Procédure nettoyage
CREATE PROCEDURE IF NOT EXISTS nettoyer_anciennes_reservations(IN nb_jours INT)
BEGIN
DELETE FROM reservations
WHERE date_fin < DATE_SUB(CURRENT_DATE(), INTERVAL nb_jours DAY)
  AND statut = 'ANNULEE';
END;

-- 8. Versionnage BDD
CREATE TABLE IF NOT EXISTS db_version (
                                          id INT PRIMARY KEY,
                                          version VARCHAR(10),
    date_mise_a_jour TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );
INSERT INTO db_version (id, version) VALUES (1, '2.0')
    ON DUPLICATE KEY UPDATE version = '2.0', date_mise_a_jour = CURRENT_TIMESTAMP;