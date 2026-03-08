package com.example.service;

import com.example.model.Reservation;
import com.example.model.StatutReservation;
import com.example.repository.ReservationRepository;
import javax.persistence.EntityManager;
import java.util.List;

public class ReservationServiceImpl implements ReservationService {

    private final EntityManager em;
    private final ReservationRepository reservationRepository;

    public ReservationServiceImpl(EntityManager em, ReservationRepository reservationRepository) {
        this.em = em;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public Reservation findById(Long id) {
        return reservationRepository.findById(id);
    }

    @Override
    public List<Reservation> findByStatut(StatutReservation statut) {
        return reservationRepository.findByStatut(statut);
    }

    @Override
    public void annuler(Long id) {
        em.getTransaction().begin();
        Reservation r = reservationRepository.findById(id);
        if (r != null) r.setStatut(StatutReservation.ANNULEE);
        em.getTransaction().commit();
    }
}