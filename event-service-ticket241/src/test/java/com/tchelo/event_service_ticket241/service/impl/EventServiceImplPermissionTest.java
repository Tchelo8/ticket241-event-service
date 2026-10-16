package com.tchelo.event_service_ticket241.service.impl;

import com.tchelo.event_service_ticket241.dto.request.CreateEventDto;
import com.tchelo.event_service_ticket241.dto.request.CreateTicketCategoryDto;
import com.tchelo.event_service_ticket241.dto.request.UpdateEventDto;
import com.tchelo.event_service_ticket241.entity.Category;
import com.tchelo.event_service_ticket241.entity.City;
import com.tchelo.event_service_ticket241.entity.Event;
import com.tchelo.event_service_ticket241.entity.Structure;
import com.tchelo.event_service_ticket241.entity.enums.EventStatus;
import com.tchelo.event_service_ticket241.exception.UnauthorizedAccesException;
import com.tchelo.event_service_ticket241.mapper.EventMapper;
import com.tchelo.event_service_ticket241.mapper.TicketCategoryMapper;
import com.tchelo.event_service_ticket241.repository.*;
import com.tchelo.event_service_ticket241.security.UserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EventServiceImplPermissionTest {

    private static final UserContext ADMIN = UserContext.of("+24100000000", "ROLE_ADMIN", null);
    private static final UserContext USER = UserContext.of("+24100000001", "ROLE_USER", null);
    private static final UserContext ADMIN_OF_STRUCTURE_1 = UserContext.of("+24100000002", "ROLE_STRUCTURE_ADMIN", "1");
    private static final UserContext ADMIN_OF_STRUCTURE_2 = UserContext.of("+24100000003", "ROLE_STRUCTURE_ADMIN", "2");

    @Mock private EventRepository eventRepository;
    @Mock private StructureRepository structureRepository;
    @Mock private TicketCategoryRepository ticketCategoryRepository;
    @Mock private CityRepository cityRepository;
    @Mock private CategoryRepository categoryRepository;

    private EventServiceImpl service;
    private Structure structure;
    private Event event;

    @BeforeEach
    void setUp() {
        TicketCategoryMapper ticketCategoryMapper = new TicketCategoryMapper();
        service = new EventServiceImpl(eventRepository, structureRepository, ticketCategoryRepository,
                cityRepository, categoryRepository, new EventMapper(ticketCategoryMapper), ticketCategoryMapper);

        structure = new Structure();
        structure.setId(1L);
        structure.setNom("Institut Français");
        structure.setIsVerified(false);

        City city = City.builder().name("Libreville").country("Gabon").eventCount(0).build();
        city.setId(5L);
        Category category = Category.builder().name("CONCERT").displayName("Concert").build();
        category.setId(7L);

        event = Event.builder()
                .name("Concert")
                .structure(structure)
                .city(city)
                .category(category)
                .eventStatus(EventStatus.DRAFT)
                .totalSeats(10).availableSeats(10).reservedSeats(0).soldSeats(0)
                .startDate(LocalDateTime.now().plusDays(10))
                .build();

        when(structureRepository.findById(1L)).thenReturn(Optional.of(structure));
        when(cityRepository.findById(5L)).thenReturn(Optional.of(city));
        when(categoryRepository.findById(7L)).thenReturn(Optional.of(category));
        when(eventRepository.findByUuid("event-1")).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private CreateEventDto newEvent(Long structureId) {
        return CreateEventDto.builder()
                .name("Concert de test").fullDescription("Un concert pour tester les droits")
                .categoryId(7L).cityId(5L).structureId(structureId)
                .startDate(LocalDateTime.now().plusDays(10))
                .venueName("Stade").totalSeats(10)
                .ticketCategories(List.of(CreateTicketCategoryDto.builder()
                        .name("Standard").price(new BigDecimal("5000")).totalSeats(10).build()))
                .build();
    }

    @Test
    void aSimpleUserCannotCreateAnEvent() {
        assertThatThrownBy(() -> service.createEvent(newEvent(1L), USER))
                .isInstanceOf(UnauthorizedAccesException.class);
    }

    @Test
    void aStructureAdminCanOnlyCreateEventsForItsOwnStructure() {
        assertThat(service.createEvent(newEvent(1L), ADMIN_OF_STRUCTURE_1)).isNotNull();
        assertThatThrownBy(() -> service.createEvent(newEvent(1L), ADMIN_OF_STRUCTURE_2))
                .isInstanceOf(UnauthorizedAccesException.class);
    }

    @Test
    void allAdminsOfTheStructureCanManageItsEvents() {
        event.setCreatedBy("un-collegue");

        assertThat(service.cancelEvent("event-1", "Report", ADMIN_OF_STRUCTURE_1).getEventStatus())
                .isEqualTo(EventStatus.CANCELLED);
        assertThatThrownBy(() -> service.cancelEvent("event-1", "Report", ADMIN_OF_STRUCTURE_2))
                .isInstanceOf(UnauthorizedAccesException.class);
    }

    @Test
    void unverifiedStructureCannotPublishAlone() {
        assertThatThrownBy(() -> service.publishEvent("event-1", ADMIN_OF_STRUCTURE_1))
                .isInstanceOf(UnauthorizedAccesException.class)
                .hasMessageContaining("vérifiée");

        structure.setIsVerified(true);
        assertThat(service.publishEvent("event-1", ADMIN_OF_STRUCTURE_1).getEventStatus())
                .isEqualTo(EventStatus.PUBLISHED);
    }

    @Test
    void platformAdminCanPublishForAnUnverifiedStructure() {
        assertThat(service.publishEvent("event-1", ADMIN).getEventStatus()).isEqualTo(EventStatus.PUBLISHED);
    }

    @Test
    void draftsAreInvisibleOnPublicPages() {
        when(eventRepository.findBySlug("concert")).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> service.getEventByUuid("event-1"))
                .isInstanceOf(com.tchelo.event_service_ticket241.exception.EventNotFoundException.class);
        assertThatThrownBy(() -> service.getEventBySlug("concert"))
                .isInstanceOf(com.tchelo.event_service_ticket241.exception.EventNotFoundException.class);
        // L'organisateur, lui, voit son brouillon par la route de gestion
        assertThat(service.getEventBySlugForManagement("concert", ADMIN_OF_STRUCTURE_1)).isNotNull();

        event.setEventStatus(EventStatus.PUBLISHED);
        assertThat(service.getEventByUuid("event-1")).isNotNull();

        // Un événement annulé reste consultable par les détenteurs de billets
        event.setEventStatus(EventStatus.CANCELLED);
        assertThat(service.getEventBySlug("concert")).isNotNull();
    }

    @Test
    void onlyThePlatformCanFeatureAnEvent() {
        UpdateEventDto featured = UpdateEventDto.builder().isFeatured(true).build();

        assertThatThrownBy(() -> service.updateEvent("event-1", featured, ADMIN_OF_STRUCTURE_1))
                .isInstanceOf(UnauthorizedAccesException.class);
        assertThat(service.updateEvent("event-1", featured, ADMIN).getIsFeatured()).isTrue();
    }
}
