package com.tchelo.event_service_ticket241.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Résultat d'une publication groupée : chaque événement est traité seul (un refus ne bloque pas les autres)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchPublishResultDto {

    private int published;
    private int refused;
    private List<Item> results;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Item {
        private String uuid;
        private String name;
        private boolean published;
        // Raison du refus (mêmes messages que la publication d'un seul événement)
        private String message;
    }
}
