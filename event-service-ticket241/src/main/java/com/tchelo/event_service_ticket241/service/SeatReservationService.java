package com.tchelo.event_service_ticket241.service;

// Mouvements de places pilotés par les messages Kafka de ticket-service. Toutes les opérations sont
// idempotentes : rejouer un message ne change rien de plus que la première fois.
public interface SeatReservationService {

    // Réserve les places d'une commande. Si la commande a déjà été traitée, renvoie le même résultat
    // sans rien modifier (permet de republier une réponse perdue).
    ReservationOutcome reserve(String orderId, Long categoryId, Integer quantity);

    // Libère la place réservée d'un billet (réservation expirée, annulée ou paiement échoué)
    void releaseReservedTicket(String orderId, Long ticketId);

    // Paiement confirmé : les places réservées de la commande deviennent vendues
    void confirmPaidOrder(String orderId, Integer quantity);

    // Billet payé puis annulé : libère une place vendue
    void releaseSoldTicket(String orderId, Long ticketId);

    record ReservationOutcome(boolean accepted, Long eventId, String message) {
    }
}
