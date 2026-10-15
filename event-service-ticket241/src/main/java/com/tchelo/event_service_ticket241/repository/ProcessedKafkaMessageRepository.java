package com.tchelo.event_service_ticket241.repository;

import com.tchelo.event_service_ticket241.entity.ProcessedKafkaMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedKafkaMessageRepository extends JpaRepository<ProcessedKafkaMessage, String> {
}
