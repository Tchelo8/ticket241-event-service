package com.tchelo.event_service_ticket241.repository;


import com.tchelo.event_service_ticket241.entity.Structure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface StructureRepository extends JpaRepository<Structure, Long> {
    boolean existsByEmail(String email);
    Structure findByUuid(String uuid);
}
