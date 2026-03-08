package com.example.repository;

import com.example.model.Salle;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class SalleRepositoryImpl implements SalleRepository {

    private final EntityManager em;

    public SalleRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public Salle findById(Long id) {
        return em.find(Salle.class, id);
    }

    @Override
    public List<Salle> findAll() {
        return em.createQuery("SELECT s FROM Salle s", Salle.class).getResultList();
    }

    @Override
    public List<Salle> findAvailableRooms(LocalDateTime debut, LocalDateTime fin) {
        return em.createQuery(
                        "SELECT DISTINCT s FROM Salle s WHERE s.id NOT IN (" +
                                "SELECT r.salle.id FROM Reservation r " +
                                "WHERE r.statut <> 'ANNULEE' " +
                                "AND r.dateDebut < :fin AND r.dateFin > :debut)", Salle.class)
                .setParameter("debut", debut)
                .setParameter("fin", fin)
                .getResultList();
    }

    @Override
    public List<Salle> searchRooms(Map<String, Object> criteres) {
        StringBuilder jpql = new StringBuilder(
                "SELECT DISTINCT s FROM Salle s LEFT JOIN s.equipements e WHERE 1=1");

        if (criteres.containsKey("capaciteMin"))
            jpql.append(" AND s.capacite >= :capaciteMin");
        if (criteres.containsKey("capaciteMax"))
            jpql.append(" AND s.capacite <= :capaciteMax");
        if (criteres.containsKey("batiment"))
            jpql.append(" AND s.batiment = :batiment");
        if (criteres.containsKey("etage"))
            jpql.append(" AND s.etage = :etage");
        if (criteres.containsKey("equipement"))
            jpql.append(" AND e.id = :equipement");

        TypedQuery<Salle> query = em.createQuery(jpql.toString(), Salle.class);

        criteres.forEach((k, v) -> query.setParameter(k, v));

        return query.getResultList();
    }

    @Override
    public List<Salle> findPaginated(int page, int pageSize) {
        return em.createQuery("SELECT s FROM Salle s ORDER BY s.id", Salle.class)
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    @Override
    public long count() {
        return em.createQuery("SELECT COUNT(s) FROM Salle s", Long.class)
                .getSingleResult();
    }

    @Override
    public void save(Salle salle) {
        em.getTransaction().begin();
        em.persist(salle);
        em.getTransaction().commit();
    }

    @Override
    public void update(Salle salle) {
        em.getTransaction().begin();
        em.merge(salle);
        em.getTransaction().commit();
    }

    @Override
    public void delete(Long id) {
        em.getTransaction().begin();
        Salle salle = em.find(Salle.class, id);
        if (salle != null) em.remove(salle);
        em.getTransaction().commit();
    }
}