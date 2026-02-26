package com.tchelo.event_service_ticket241.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCityDto {

    @NotBlank(message = "Le nom de la ville est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String name;

    @NotBlank(message = "Le pays est obligatoire")
    @Size(min = 2, max = 50, message = "Le pays doit contenir entre 2 et 50 caractères")
    private String country;

    @Pattern(regexp = "^[A-Z]{2,3}$", message = "Le code pays doit être en majuscules (2-3 lettres)")
    private String countryCode;

    @Size(max = 100, message = "La région ne peut pas dépasser 100 caractères")
    private String region;

    @Size(max = 20, message = "Le code postal ne peut pas dépasser 20 caractères")
    private String postalCode;

    private Double latitude;

    private Double longitude;
}
