package com.tchelo.event_service_ticket241.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Favoris gardés sur l'appareil avant la connexion, versés sur le compte (uuid des événements)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportFavoritesRequestDto {

    @NotNull(message = "Liste des événements manquante")
    @Size(max = 100, message = "100 favoris au maximum par import")
    private List<@NotBlank(message = "Identifiant d'événement vide") String> eventUuids;
}
