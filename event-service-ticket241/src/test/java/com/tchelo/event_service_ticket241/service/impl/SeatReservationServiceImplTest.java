package com.tchelo.event_service_ticket241.service.impl;

import com.tchelo.event_service_ticket241.entity.Event;
import com.tchelo.event_service_ticket241.entity.ProcessedKafkaMessage;
import com.tchelo.event_service_ticket241.entity.SeatReservation;
import com.tchelo.event_service_ticket241.entity.TicketCategory;
import com.tchelo.event_service_ticket241.entity.enums.EventStatus;
import com.tchelo.event_service_ticket241.entity.enums.SeatReservationStatus;
import com.tchelo.event_service_ticket241.repository.EventRepository;
import com.tchelo.event_service_ticket241.repository.ProcessedKafkaMessageRepository;
import com.tchelo.event_service_ticket241.repository.SeatReservationRepository;
import com.tchelo.event_service_ticket241.repository.TicketCategoryRepository;
import com.tchelo.event_service_ticket241.service.SeatReservationService.ReservationOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SeatReservationServiceImplTest {

    private static final Long EVENT_ID = 1L;
    private static final Long VIP_ID = 10L;
    private static final Long STANDARD_ID = 11L;

    @Mock
    private SeatReservationRepository seatReservationRepository;
    @Mock
    private ProcessedKafkaMessageRepository processedKafkaMessageRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private TicketCategoryRepository ticketCategoryRepository;

    private SeatReservationServiceImpl service;

    private Event event;
    private TicketCategory vip;
    private TicketCategory standard;

    // "Base" en mémoire pour les réservations et les messages traités
    private final Map<String, SeatReservation> reservations = new HashMap<>();
    private final Set<String> processedKeys = new HashSet<>();

    @BeforeEach
    void setUp() {
        service = new SeatReservationServiceImpl(seatReservationRepository, processedKafkaMessageRepository,
                eventRepository, ticketCategoryRepository);

        event = Event.builder()
                .name("Concert")
                .eventStatus(EventStatus.PUBLISHED)
                .isActive(true)
                .isCancelled(false)
                .startDate(LocalDateTime.now().plusDays(10))
                .totalSeats(15)
                .availableSeats(15)
                .reservedSeats(0)
                .soldSeats(0)
                .build();
        event.setId(EVENT_ID);

        vip = category(VIP_ID, "VIP", 5);
        standard = category(STANDARD_ID, "Standard", 10);
        event.addTicketCategory(vip);
        event.addTicketCategory(standard);

        when(eventRepository.findByIdForUpdate(EVENT_ID)).thenReturn(Optional.of(event));
        when(ticketCategoryRepository.findEventIdByCategoryId(VIP_ID)).thenReturn(Optional.of(EVENT_ID));
        when(ticketCategoryRepository.findEventIdByCategoryId(STANDARD_ID)).thenReturn(Optional.of(EVENT_ID));
        when(ticketCategoryRepository.findByIdForUpdate(VIP_ID)).thenReturn(Optional.of(vip));
        when(ticketCategoryRepository.findByIdForUpdate(STANDARD_ID)).thenReturn(Optional.of(standard));

        when(seatReservationRepository.findByOrderIdForUpdate(anyString()))
                .thenAnswer(inv -> Optional.ofNullable(reservations.get(inv.getArgument(0, String.class))));
        when(seatReservationRepository.save(any(SeatReservation.class))).thenAnswer(inv -> {
            SeatReservation r = inv.getArgument(0);
            reservations.put(r.getOrderId(), r);
            return r;
        });
        when(processedKafkaMessageRepository.existsById(anyString()))
                .thenAnswer(inv -> processedKeys.contains(inv.getArgument(0, String.class)));
        when(processedKafkaMessageRepository.save(any(ProcessedKafkaMessage.class))).thenAnswer(inv -> {
            ProcessedKafkaMessage m = inv.getArgument(0);
            processedKeys.add(m.getMessageKey());
            return m;
        });
    }

    private TicketCategory category(Long id, String name, int seats) {
        TicketCategory category = TicketCategory.builder()
                .name(name)
                .totalSeats(seats)
                .availableSeats(seats)
                .maxTicketsPerOrder(10)
                .build();
        category.setId(id);
        return category;
    }

    @Test
    void reserveHoldsSeatsAndUpdatesEventCounters() {
        ReservationOutcome outcome = service.reserve("order-1", VIP_ID, 2);

        assertThat(outcome.accepted()).isTrue();
        assertThat(vip.getReservedSeats()).isEqualTo(2);
        assertThat(vip.getAvailableSeats()).isEqualTo(3);
        assertThat(event.getReservedSeats()).isEqualTo(2);
        assertThat(event.getAvailableSeats()).isEqualTo(13);
        assertThat(reservations.get("order-1").getStatus()).isEqualTo(SeatReservationStatus.ACCEPTED);
    }

    @Test
    void replayedReservationDoesNotReserveTwice() {
        service.reserve("order-1", VIP_ID, 2);
        ReservationOutcome replay = service.reserve("order-1", VIP_ID, 2);

        assertThat(replay.accepted()).isTrue();
        assertThat(vip.getReservedSeats()).isEqualTo(2);
    }

    @Test
    void reservationIsRefusedWhenNotEnoughSeats() {
        ReservationOutcome outcome = service.reserve("order-1", VIP_ID, 6);

        assertThat(outcome.accepted()).isFalse();
        assertThat(outcome.message()).contains("Plus assez de places");
        assertThat(vip.getReservedSeats()).isZero();
        assertThat(reservations.get("order-1").getStatus()).isEqualTo(SeatReservationStatus.REJECTED);
    }

    @Test
    void reservationIsRefusedWhenEventIsNotPublished() {
        event.setEventStatus(EventStatus.DRAFT);

        ReservationOutcome outcome = service.reserve("order-1", VIP_ID, 1);

        assertThat(outcome.accepted()).isFalse();
        assertThat(outcome.message()).contains("pas ouvert à la vente");
        assertThat(vip.getReservedSeats()).isZero();
    }

    @Test
    void releasingAReservedTicketGivesBackOneSeatOnlyOnce() {
        service.reserve("order-1", VIP_ID, 2);

        service.releaseReservedTicket("order-1", 100L);
        service.releaseReservedTicket("order-1", 100L); // message rejoué

        assertThat(vip.getReservedSeats()).isEqualTo(1);
        assertThat(vip.getAvailableSeats()).isEqualTo(4);
        assertThat(reservations.get("order-1").getReservedQuantity()).isEqualTo(1);
    }

    @Test
    void expiryArrivingBeforeReservationClosesTheOrder() {
        service.releaseReservedTicket("order-1", 100L);
        ReservationOutcome lateReservation = service.reserve("order-1", VIP_ID, 2);

        assertThat(lateReservation.accepted()).isFalse();
        assertThat(vip.getReservedSeats()).isZero();
        assertThat(reservations.get("order-1").getStatus()).isEqualTo(SeatReservationStatus.CLOSED);
    }

    @Test
    void paymentTurnsReservedSeatsIntoSoldSeats() {
        service.reserve("order-1", VIP_ID, 2);

        service.confirmPaidOrder("order-1", 2);
        service.confirmPaidOrder("order-1", 2); // message rejoué

        assertThat(vip.getReservedSeats()).isZero();
        assertThat(vip.getSoldSeats()).isEqualTo(2);
        assertThat(vip.getAvailableSeats()).isEqualTo(3);
        assertThat(event.getSoldSeats()).isEqualTo(2);
    }

    @Test
    void cancellingAPaidTicketReleasesASoldSeat() {
        service.reserve("order-1", VIP_ID, 2);
        service.confirmPaidOrder("order-1", 2);

        service.releaseSoldTicket("order-1", 100L);

        assertThat(vip.getSoldSeats()).isEqualTo(1);
        assertThat(vip.getAvailableSeats()).isEqualTo(4);
    }

    @Test
    void releasingReservedSeatAfterPaymentDoesNothing() {
        service.reserve("order-1", VIP_ID, 1);
        service.confirmPaidOrder("order-1", 1);

        // reservation-expired en retard pour un billet déjà payé : ne doit rien libérer
        service.releaseReservedTicket("order-1", 100L);

        assertThat(vip.getSoldSeats()).isEqualTo(1);
        assertThat(vip.getAvailableSeats()).isEqualTo(4);
    }

    @Test
    void eventCountersSumAllCategories() {
        service.reserve("order-1", VIP_ID, 2);
        service.reserve("order-2", STANDARD_ID, 3);
        service.confirmPaidOrder("order-2", 3);

        assertThat(event.getReservedSeats()).isEqualTo(2);
        assertThat(event.getSoldSeats()).isEqualTo(3);
        assertThat(event.getAvailableSeats()).isEqualTo(10);
    }

    @Test
    void paymentForUnknownOrderSellsNothing() {
        service.confirmPaidOrder("unknown-order", 2);

        assertThat(vip.getSoldSeats()).isZero();
        verify(eventRepository, never()).findByIdForUpdate(EVENT_ID);
    }

    @Test
    void rejectionIsPersistedWithItsReason() {
        service.reserve("order-1", VIP_ID, 0);

        ArgumentCaptor<SeatReservation> saved = ArgumentCaptor.forClass(SeatReservation.class);
        verify(seatReservationRepository).save(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo(SeatReservationStatus.REJECTED);
        assertThat(saved.getValue().getFailureReason()).isEqualTo("Quantité de billets invalide");
    }
}
