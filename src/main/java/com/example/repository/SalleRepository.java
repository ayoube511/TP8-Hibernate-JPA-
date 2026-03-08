package com.example.repository;

import com.example.model.Salle;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface SalleRepository {
    Salle findById(Long id);
    List<Salle> findAll();
    List<Salle> findAvailableRooms(LocalDateTime debut, LocalDateTime fin);
    List<Salle> searchRooms(Map<String, Object> criteres);
    List<Salle> findPaginated(int page, int pageSize);
    long count();
    void save(Salle salle);
    void update(Salle salle);
    void delete(Long id);
}