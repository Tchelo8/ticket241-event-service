package com.tchelo.event_service_ticket241.repository;

import com.tchelo.event_service_ticket241.entity.Event;
import com.tchelo.event_service_ticket241.entity.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByUuid(String uuid);
    Optional<Event> findBySlug(String slug);
    boolean existsBySlug(String slug);

    // Verrou posé en premier sur tout mouvement de places : sérialise les réservations d'un même
    // événement, pour que ses compteurs (somme des catégories) soient recalculés sur des données fraîches
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Event e WHERE e.id = :id")
    Optional<Event> findByIdForUpdate(@Param("id") Long id);

    // Listage des évènements publics et actifs — page d'accueil

    @Query("SELECT e FROM Event e WHERE e.isPublic = true AND e.isActive = true AND e.eventStatus = :status ORDER BY e.startDate ASC")
    Page<Event> findPublicEvents(@Param("status") EventStatus status, Pageable pageable);

    // Listage des évènements par catégorie (par le nom technique, ex: "CONCERT")
    Page<Event> findByCategoryNameIgnoreCaseAndIsPublicTrueAndIsActiveTrueAndEventStatus(
            String categoryName,
            EventStatus status,
            Pageable pageable
    );

    // Listage des évènements par ville
    Page<Event> findByCityIdAndIsPublicTrueAndIsActiveTrueAndEventStatus(
            Long cityId,
            EventStatus status,
            Pageable pageable
    );

    // Recherche par objet City complet
    Page<Event> findByCityAndIsPublicTrueAndIsActiveTrueAndEventStatus(
            com.tchelo.event_service_ticket241.entity.City city,
            EventStatus status,
            Pageable pageable
    );

    // Recherche par mot-clé (nom, description ou ville)
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

    // Événements à venir (startDate >= maintenant)

    @Query("SELECT e FROM Event e WHERE e.startDate >= :now AND e.isPublic = true AND e.isActive = true AND e.eventStatus = :status ORDER BY e.startDate ASC")
    Page<Event> findUpcomingEvents(
            @Param("now") LocalDateTime now,
            @Param("status") EventStatus status,
            Pageable pageable
    );

    // Événements actifs d'une structure
    Page<Event> findByStructureIdAndIsActiveTrue(Long structureId, Pageable pageable);

    // Événements actifs créés par un utilisateur
    Page<Event> findByCreatedByAndIsActiveTrue(String userId, Pageable pageable);

    // Événements mis en avant sur la plateforme

    @Query("SELECT e FROM Event e WHERE e.isFeatured = true AND e.isPublic = true AND e.isActive = true AND e.eventStatus = :status ORDER BY e.publishedAt DESC")
    Page<Event> findFeaturedEvents(@Param("status") EventStatus status, Pageable pageable);

    // Compte le nombre d'événements d'une structure
    long countByStructureIdAndIsActiveTrue(Long structureId);

    // Une structure qui a des événements (même supprimés logiquement) ne peut pas être supprimée
    boolean existsByStructureId(Long structureId);

    // Compte les événements d'une ville
    long countByCityIdAndIsActiveTrue(Long cityId);

    // Incrémente le compteur de vues de façon atomique (évite les pertes de vues en cas d'accès concurrents)
    @Modifying
    @Query("UPDATE Event e SET e.viewCount = e.viewCount + 1 WHERE e.uuid = :uuid")
    int incrementViewCount(@Param("uuid") String uuid);
}