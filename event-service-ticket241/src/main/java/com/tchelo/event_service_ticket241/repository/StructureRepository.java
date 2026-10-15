package com.tchelo.event_service_ticket241.repository;

import com.tchelo.event_service_ticket241.entity.Structure;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StructureRepository extends JpaRepository<Structure, Long> {
    boolean existsByEmail(String email);
    Structure findByUuid(String uuid);

    // Verrouillée pendant la consommation d'un code d'invitation : deux inscriptions simultanées
    // avec le même code ne peuvent pas toutes les deux réussir
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Structure s WHERE s.id = :id")
    Optional<Structure> findByIdForUpdate(@Param("id") Long id);
}
