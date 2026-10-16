package com.tchelo.event_service_ticket241.service;

import java.util.List;

// Mouvements de places pilotés par les messages Kafka de ticket-service. Toutes les opérations sont
// idempotentes : rejouer un message ne change rien de plus que la première fois.
// Une commande peut contenir plusieurs catégories : ses places sont réservées en tout-ou-rien.
public interface SeatReservationService {

    // Réserve les places d'une commande (toutes ses catégories, ou aucune). Si la commande a déjà été
    // traitée, renvoie le même résultat sans rien modifier (permet de republier une réponse perdue).
    ReservationOutcome reserve(String orderId, List<ReservationLine> lines);

    default ReservationOutcome reserve(String orderId, Long categoryId, Integer quantity) {
        return reserve(orderId, List.of(new ReservationLine(categoryId, quantity)));
    }

    // Libère la place réservée d'un billet (réservation expirée, annulée ou paiement échoué).
    // categoryId null (ancien message) : accepté seulement pour une commande d'une seule catégorie.
    void releaseReservedTicket(String orderId, Long ticketId, Long categoryId);

    default void releaseReservedTicket(String orderId, Long ticketId) {
        releaseReservedTicket(orderId, ticketId, null);
    }

    // Paiement confirmé : les places réservées payées deviennent vendues, catégorie par catégorie.
    // paidLines null : toutes les places encore réservées de la commande.
    void confirmPaidOrder(String orderId, List<ReservationLine> paidLines);

    default void confirmPaidOrder(String orderId, Integer quantity) {
        confirmPaidOrder(orderId, quantity == null ? null : List.of(new ReservationLine(null, quantity)));
    }

    // Billet payé puis annulé : libère une place vendue de sa catégorie
    void releaseSoldTicket(String orderId, Long ticketId, Long categoryId);

    default void releaseSoldTicket(String orderId, Long ticketId) {
        releaseSoldTicket(orderId, ticketId, null);
    }

    record ReservationLine(Long categoryId, Integer quantity) {
    }

    record ReservationOutcome(boolean accepted, Long eventId, String message) {
    }
}
