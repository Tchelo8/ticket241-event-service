package com.tchelo.event_service_ticket241.repository;

import com.tchelo.event_service_ticket241.entity.EventFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface EventFavoriteRepository extends JpaRepository<EventFavorite, Long> {

    // Événement, ville et catégorie chargés d'un coup : la liste est convertie en EventSummaryDto
    @Query("SELECT f FROM EventFavorite f JOIN FETCH f.event e LEFT JOIN FETCH e.city LEFT JOIN FETCH e.category "
            + "WHERE f.userId = :userId ORDER BY f.createdAt DESC")
    List<EventFavorite> findAllWithEventByUserId(@Param("userId") String userId);

    @Query("SELECT f.event.id FROM EventFavorite f WHERE f.userId = :userId")
    Set<Long> findEventIdsByUserId(@Param("userId") String userId);

    boolean existsByUserIdAndEventId(String userId, Long eventId);

    long countByUserId(String userId);

    @Modifying
    @Query("DELETE FROM EventFavorite f WHERE f.userId = :userId AND f.event.uuid = :eventUuid")
    int deleteByUserIdAndEventUuid(@Param("userId") String userId, @Param("eventUuid") String eventUuid);
}
