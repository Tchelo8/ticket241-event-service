package com.tchelo.event_service_ticket241.repository;
import com.tchelo.event_service_ticket241.entity.TicketCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketCategoryRepository extends JpaRepository<TicketCategory, Long> {

    Optional<TicketCategory> findByUuid(UUID uuid);

    // liste des catégories d'un événement (triées par ordre d'affichage)
    List<TicketCategory> findByEventIdAndIsActiveTrueOrderByDisplayOrderAsc(Long eventId);

    // vérifie si une catégorie existe pour un événement avec un nom donné
    boolean existsByEventIdAndNameIgnoreCase(Long eventId, String name);

    // compte le nombre de catégories actives d'un événement
    long countByEventIdAndIsActiveTrue(Long eventId);

    // addition des places totales pour un évènement
    @Query("SELECT COALESCE(SUM(tc.totalSeats), 0) FROM TicketCategory tc WHERE tc.event.id = :eventId AND tc.isActive = true")
    Integer sumTotalSeatsByEventId(@Param("eventId") Long eventId);

    // addition des places disponibles pour un événement
    @Query("SELECT COALESCE(SUM(tc.availableSeats), 0) FROM TicketCategory tc WHERE tc.event.id = :eventId AND tc.isActive = true")
    Integer sumAvailableSeatsByEventId(@Param("eventId") Long eventId);
}
