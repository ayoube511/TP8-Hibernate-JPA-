package com.example.service;

import com.example.model.Reservation;
import com.example.model.StatutReservation;
import java.util.List;

public interface ReservationService {
    Reservation findById(Long id);
    List<Reservation> findByStatut(StatutReservation statut);
    void annuler(Long id);
}