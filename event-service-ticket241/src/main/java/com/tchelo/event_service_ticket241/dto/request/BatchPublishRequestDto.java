package com.tchelo.event_service_ticket241.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Publication de plusieurs brouillons en une fois (uuid des événements)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchPublishRequestDto {

    @NotEmpty(message = "Indiquez au moins un événement à publier")
    @Size(max = 100, message = "100 événements au maximum par publication groupée")
    private List<@NotBlank(message = "Identifiant d'événement vide") String> eventUuids;
}
