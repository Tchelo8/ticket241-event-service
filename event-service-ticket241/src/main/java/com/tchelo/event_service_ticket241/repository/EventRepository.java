package com.tchelo.event_service_ticket241.repository;

import com.tchelo.event_service_ticket241.entity.Event;
import com.tchelo.event_service_ticket241.entity.enums.EventCategory;
import com.tchelo.event_service_ticket241.entity.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByUuid(String uuid);
    Optional<Event> findBySlug(String slug);
    boolean existsBySlug(String slug);

    // listage des évènements publics et actifs pour la page d'acceuil de la plateforme
    @Query("SELECT e FROM Event e WHERE e.isPublic = true AND e.isActive = true AND e.status = :status ORDER BY e.startDate ASC")
    Page<Event> findPublicEvents(@Param("status") EventStatus status, Pageable pageable);

    //listage des évènements par catégorie
    Page<Event> findByCategoryAndIsPublicTrueAndIsActiveTrueAndStatus(
            EventCategory category,
            EventStatus status,
            Pageable pageable
    );

    //listage des évènements par ville utile lors du changement de ville
    Page<Event> findByCityIdAndIsPublicTrueAndIsActiveTrueAndEventStatus(
            Long cityId,
            EventStatus status,
            Pageable pageable
    );

    // recherche par objet City complet
    Page<Event> findByCityAndIsPublicTrueAndIsActiveTrueAndEventStatus(
            com.tchelo.event_service_ticket241.entity.City city,
            EventStatus status,
            Pageable pageable
    );


     // recherche d'événements par mot-clé (nom, description ou ville)
    @Query("SELECT e FROM Event e WHERE " +
            "(LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(e.fullDescription) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(e.city.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND e.isPublic = true AND e.isActive = true AND e.eventStatus = :status")
    Page<Event> searchEvents(
            @Param("keyword") String keyword,
            @Param("status") EventStatus status,
            Pageable pageable
    );

    // événements à venir (date >= aujourd'hui)
    @Query("SELECT e FROM Event e WHERE e.startDate >= :now AND e.isPublic = true AND e.isActive = true AND e.status = :status ORDER BY e.startDate ASC")
    Page<Event> findUpcomingEvents(
            @Param("now") LocalDateTime now,
            @Param("status") EventStatus status,
            Pageable pageable
    );

   //evenement actifs d'une structure
    Page<Event> findByStructureIdAndIsActiveTrue(Long structureId, Pageable pageable);

   // evenement actif creer par un utilisateur actif
    Page<Event> findByCreatedByAndIsActiveTrue(String userId, Pageable pageable);

    // evenement mis en avant sur la plateforme
    @Query("SELECT e FROM Event e WHERE e.isFeatured = true AND e.isPublic = true AND e.isActive = true AND e.status = :status ORDER BY e.publishedAt DESC")
    Page<Event> findFeaturedEvents(@Param("status") EventStatus status, Pageable pageable);


     //compte le nombre d'événements d'une structure
    long countByStructureIdAndIsActiveTrue(Long structureId);

    //compte les événements d'une ville
    long countByCityIdAndIsActiveTrue(Long cityId);
}
