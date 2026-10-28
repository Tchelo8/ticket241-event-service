package com.tchelo.event_service_ticket241.service.impl;

import com.tchelo.event_service_ticket241.dto.response.EventSummaryDto;
import com.tchelo.event_service_ticket241.entity.Event;
import com.tchelo.event_service_ticket241.entity.EventFavorite;
import com.tchelo.event_service_ticket241.exception.EventNotFoundException;
import com.tchelo.event_service_ticket241.exception.InvalidEventDataException;
import com.tchelo.event_service_ticket241.mapper.EventMapper;
import com.tchelo.event_service_ticket241.repository.EventFavoriteRepository;
import com.tchelo.event_service_ticket241.repository.EventRepository;
import com.tchelo.event_service_ticket241.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    // Garde-fou contre les abus : bien au-delà d'un usage normal
    static final int MAX_FAVORITES = 500;

    private final EventFavoriteRepository favoriteRepository;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Override
    @Transactional(readOnly = true)
    public List<EventSummaryDto> getFavorites(String userId) {
        // Un favori dont l'événement est repassé en brouillon ou supprimé est masqué (pas effacé)
        return favoriteRepository.findAllWithEventByUserId(userId).stream()
                .map(EventFavorite::getEvent)
                .filter(Event::isPubliclyVisible)
                .map(eventMapper::toSummaryDto)
                .toList();
    }

    // Sans transaction englobante : un double clic simultané bute sur la contrainte d'unicité,
    // qu'on ignore (le favori existe déjà), sans faire échouer la requête
    @Override
    public void addFavorite(String userId, String eventUuid) {
        Event event = findVisibleEvent(eventUuid);
        if (favoriteRepository.existsByUserIdAndEventId(userId, event.getId())) {
            return;
        }
        if (favoriteRepository.countByUserId(userId) >= MAX_FAVORITES) {
            throw new InvalidEventDataException(MAX_FAVORITES + " favoris au maximum : retirez-en avant d'en ajouter.");
        }
        try {
            favoriteRepository.save(EventFavorite.builder().userId(userId).event(event).build());
        } catch (DataIntegrityViolationException alreadyAdded) {
            log.debug("Favori {} déjà enregistré pour {}", eventUuid, userId);
        }
    }

    @Override
    @Transactional
    public void removeFavorite(String userId, String eventUuid) {
        favoriteRepository.deleteByUserIdAndEventUuid(userId, eventUuid);
    }

    @Override
    @Transactional
    public List<EventSummaryDto> importFavorites(String userId, List<String> eventUuids) {
        Set<Long> existing = favoriteRepository.findEventIdsByUserId(userId);
        int room = MAX_FAVORITES - existing.size();
        for (String uuid : new LinkedHashSet<>(eventUuids)) {
            if (room <= 0) {
                break;
            }
            Event event = eventRepository.findByUuid(uuid).filter(Event::isPubliclyVisible).orElse(null);
            if (event == null || !existing.add(event.getId())) {
                continue;
            }
            favoriteRepository.save(EventFavorite.builder().userId(userId).event(event).build());
            room--;
        }
        return getFavorites(userId);
    }

    private Event findVisibleEvent(String eventUuid) {
        return eventRepository.findByUuid(eventUuid)
                .filter(Event::isPubliclyVisible)
                .orElseThrow(() -> new EventNotFoundException("Événement introuvable : " + eventUuid));
    }
}
