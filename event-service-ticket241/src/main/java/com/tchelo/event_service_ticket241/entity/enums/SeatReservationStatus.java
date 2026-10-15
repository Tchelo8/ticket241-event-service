package com.tchelo.event_service_ticket241.entity.enums;

public enum SeatReservationStatus {
    ACCEPTED,   // Places réservées pour la commande (puis éventuellement vendues ou libérées)
    REJECTED,   // Réservation refusée (plus assez de places, catégorie inactive…)
    CLOSED      // Commande expirée/annulée avant que la réservation n'arrive : toute réservation tardive est refusée
}
