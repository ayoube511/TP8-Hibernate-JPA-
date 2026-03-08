package com.example.repository;

import com.example.model.Reservation;
import com.example.model.StatutReservation;
import javax.persistence.EntityManager;
import java.util.List;

public class ReservationRepositoryImpl implements ReservationRepository {

    private final EntityManager em;

    public ReservationRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public Reservation findById(Long id) {
        return em.find(Reservation.class, id);
    }

    @Override
    public List<Reservation> findAll() {
        return em.createQuery("SELECT r FROM Reservation r", Reservation.class).getResultList();
    }

    @Override
    public List<Reservation> findByStatut(StatutReservation statut) {
        return em.createQuery(
                        "SELECT r FROM Reservation r WHERE r.statut = :statut", Reservation.class)
                .setParameter("statut", statut)
                .getResultList();
    }

    @Override
    public void save(Reservation reservation) {
        em.getTransaction().begin();
        em.persist(reservation);
        em.getTransaction().commit();
    }

    @Override
    public void update(Reservation reservation) {
        em.getTransaction().begin();
        em.merge(reservation);
        em.getTransaction().commit();
    }

    @Override
    public void delete(Long id) {
        em.getTransaction().begin();
        Reservation r = em.find(Reservation.class, id);
        if (r != null) em.remove(r);
        em.getTransaction().commit();
    }
}