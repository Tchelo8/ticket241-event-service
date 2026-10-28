package com.tchelo.event_service_ticket241.service.impl;

import com.tchelo.event_service_ticket241.dto.response.EventSummaryDto;
import com.tchelo.event_service_ticket241.entity.Event;
import com.tchelo.event_service_ticket241.entity.EventFavorite;
import com.tchelo.event_service_ticket241.entity.enums.EventStatus;
import com.tchelo.event_service_ticket241.exception.EventNotFoundException;
import com.tchelo.event_service_ticket241.exception.InvalidEventDataException;
import com.tchelo.event_service_ticket241.mapper.EventMapper;
import com.tchelo.event_service_ticket241.mapper.TicketCategoryMapper;
import com.tchelo.event_service_ticket241.repository.EventFavoriteRepository;
import com.tchelo.event_service_ticket241.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FavoriteServiceImplTest {

    private static final String USER = "+24100000001";

    @Mock
    private EventFavoriteRepository favoriteRepository;
    @Mock
    private EventRepository eventRepository;

    private FavoriteServiceImpl service;
    private Event published;
    private Event draft;

    @BeforeEach
    void setUp() {
        service = new FavoriteServiceImpl(favoriteRepository, eventRepository, new EventMapper(mock(TicketCategoryMapper.class)));
        published = event(1L, "uuid-published", EventStatus.PUBLISHED);
        draft = event(2L, "uuid-draft", EventStatus.DRAFT);
        when(eventRepository.findByUuid("uuid-published")).thenReturn(Optional.of(published));
        when(eventRepository.findByUuid("uuid-draft")).thenReturn(Optional.of(draft));
        when(eventRepository.findByUuid("uuid-unknown")).thenReturn(Optional.empty());
    }

    private static Event event(Long id, String uuid, EventStatus status) {
        Event event = new Event();
        event.setId(id);
        event.setUuid(uuid);
        event.setName("Événement " + id);
        event.setEventStatus(status);
        event.setIsActive(true);
        event.setIsPublic(true);
        event.setAvailableSeats(100);
        event.setStartDate(LocalDateTime.now().plusDays(10));
        return event;
    }

    @Test
    void addingAFavoriteSavesItOnce() {
        service.addFavorite(USER, "uuid-published");

        ArgumentCaptor<EventFavorite> saved = ArgumentCaptor.forClass(EventFavorite.class);
        verify(favoriteRepository).save(saved.capture());
        assertThat(saved.getValue().getUserId()).isEqualTo(USER);
        assertThat(saved.getValue().getEvent()).isSameAs(published);

        when(favoriteRepository.existsByUserIdAndEventId(USER, 1L)).thenReturn(true);
        service.addFavorite(USER, "uuid-published");
        verify(favoriteRepository, times(1)).save(any());
    }

    @Test
    void aHiddenOrUnknownEventCannotBeAdded() {
        assertThatThrownBy(() -> service.addFavorite(USER, "uuid-draft")).isInstanceOf(EventNotFoundException.class);
        assertThatThrownBy(() -> service.addFavorite(USER, "uuid-unknown")).isInstanceOf(EventNotFoundException.class);
        verify(favoriteRepository, never()).save(any());
    }

    @Test
    void theNumberOfFavoritesIsCapped() {
        when(favoriteRepository.countByUserId(USER)).thenReturn((long) FavoriteServiceImpl.MAX_FAVORITES);

        assertThatThrownBy(() -> service.addFavorite(USER, "uuid-published")).isInstanceOf(InvalidEventDataException.class);
        verify(favoriteRepository, never()).save(any());
    }

    @Test
    void aSimultaneousDuplicateIsIgnored() {
        when(favoriteRepository.save(any())).thenThrow(new DataIntegrityViolationException("uk_favorite_user_event"));

        assertThatCode(() -> service.addFavorite(USER, "uuid-published")).doesNotThrowAnyException();
    }

    @Test
    void favoritesOfEventsNoLongerVisibleAreHidden() {
        when(favoriteRepository.findAllWithEventByUserId(USER)).thenReturn(List.of(
                EventFavorite.builder().userId(USER).event(published).build(),
                EventFavorite.builder().userId(USER).event(draft).build()));

        List<EventSummaryDto> favorites = service.getFavorites(USER);

        assertThat(favorites).extracting(EventSummaryDto::getUuid).containsExactly("uuid-published");
    }

    @Test
    void importKeepsOnlyNewVisibleEvents() {
        Event other = event(3L, "uuid-other", EventStatus.PUBLISHED);
        when(eventRepository.findByUuid("uuid-other")).thenReturn(Optional.of(other));
        // uuid-published est déjà en favori sur le compte
        when(favoriteRepository.findEventIdsByUserId(USER)).thenReturn(new HashSet<>(Set.of(1L)));

        service.importFavorites(USER, List.of("uuid-published", "uuid-draft", "uuid-unknown", "uuid-other", "uuid-other"));

        ArgumentCaptor<EventFavorite> saved = ArgumentCaptor.forClass(EventFavorite.class);
        verify(favoriteRepository, times(1)).save(saved.capture());
        assertThat(saved.getValue().getEvent()).isSameAs(other);
    }
}
