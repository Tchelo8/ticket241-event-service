package com.tchelo.event_service_ticket241.repository;

import com.tchelo.event_service_ticket241.entity.SeatReservation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeatReservationRepository extends JpaRepository<SeatReservation, String> {

    // Verrouillée le temps du traitement d'un message de la commande
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM SeatReservation r WHERE r.orderId = :orderId")
    Optional<SeatReservation> findByOrderIdForUpdate(@Param("orderId") String orderId);
}
