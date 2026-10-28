package com.tchelo.event_service_ticket241.service;

import com.tchelo.event_service_ticket241.dto.response.EventSummaryDto;

import java.util.List;

// Favoris d'un compte connecté (userId = X-User-Id, transmis par la gateway)
public interface FavoriteService {

    // Favoris encore visibles publiquement, du plus récent au plus ancien
    List<EventSummaryDto> getFavorites(String userId);

    // Idempotent : ajouter un favori déjà présent ne change rien
    void addFavorite(String userId, String eventUuid);

    // Idempotent : retirer un favori absent ne change rien
    void removeFavorite(String userId, String eventUuid);

    // Verse les favoris de l'appareil sur le compte ; les événements inconnus ou masqués sont ignorés
    List<EventSummaryDto> importFavorites(String userId, List<String> eventUuids);
}
