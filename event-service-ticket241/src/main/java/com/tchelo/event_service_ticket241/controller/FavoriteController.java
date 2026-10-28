package com.tchelo.event_service_ticket241.controller;

import com.tchelo.event_service_ticket241.dto.request.ImportFavoritesRequestDto;
import com.tchelo.event_service_ticket241.dto.response.EventSummaryDto;
import com.tchelo.event_service_ticket241.service.FavoriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Favoris du compte connecté. Route protégée de la gateway (JWT obligatoire) : X-User-Id est
// toujours fourni par la gateway, jamais par le client.
@RestController
@RequestMapping("/api/events/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    public ResponseEntity<List<EventSummaryDto>> getFavorites(
            @RequestHeader(value = "X-User-Id", required = true) String userId
    ) {
        return ResponseEntity.ok(favoriteService.getFavorites(userId));
    }

    @PutMapping("/{eventUuid}")
    public ResponseEntity<Void> addFavorite(
            @PathVariable String eventUuid,
            @RequestHeader(value = "X-User-Id", required = true) String userId
    ) {
        favoriteService.addFavorite(userId, eventUuid);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{eventUuid}")
    public ResponseEntity<Void> removeFavorite(
            @PathVariable String eventUuid,
            @RequestHeader(value = "X-User-Id", required = true) String userId
    ) {
        favoriteService.removeFavorite(userId, eventUuid);
        return ResponseEntity.noContent().build();
    }

    // Appelé juste après la connexion : renvoie la liste complète du compte
    @PostMapping("/import")
    public ResponseEntity<List<EventSummaryDto>> importFavorites(
            @Valid @RequestBody ImportFavoritesRequestDto request,
            @RequestHeader(value = "X-User-Id", required = true) String userId
    ) {
        return ResponseEntity.ok(favoriteService.importFavorites(userId, request.getEventUuids()));
    }
}
