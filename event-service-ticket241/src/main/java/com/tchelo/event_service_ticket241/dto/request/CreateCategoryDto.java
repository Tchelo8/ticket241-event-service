package com.tchelo.event_service_ticket241.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCategoryDto {

    @NotBlank(message = "Le nom de la catégorie est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String name;

    @NotBlank(message = "Le nom d'affichage de la catégorie est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom d'affichage doit contenir entre 2 et 100 caractères")
    private String displayName;

    @Size(max = 250, message = "La description ne peut pas dépasser 250 caractères")
    private String description;

    @Size(max = 500, message = "L'URL de l'icône ne peut pas dépasser 500 caractères")
    private String iconUrl;
}
