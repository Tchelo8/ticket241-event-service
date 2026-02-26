package com.tchelo.event_service_ticket241.dto.request;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTicketCategoryDto {

    @NotBlank(message = "Le nom de la catégorie est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String name;

    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères, essayez de réduire")
    private String description;

    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix doit être supérieur à 0")
    @Digits(integer = 8, fraction = 2, message = "Le prix ne peut avoir plus de 8 chiffres avant la virgule et 2 après")
    private BigDecimal price;

    @NotNull(message = "Le nombre de places est obligatoire pour cette catégorie")
    @Min(value = 1, message = "Le nombre de places doit être au moins de 1 place")
    private Integer totalSeats;

    @Min(value = 0, message = "L'ordre d'affichage ne peut pas être négatif")
    private Integer displayOrder = 0;

    @Min(value = 1, message = "Le nombre maximum de tickets par commande doit être au moins de 1 ticket")
    private Integer maxTicketsPerOrder = 10;

    @Size(max = 500, message = "Les avantages ne peuvent pas dépasser 500 caractères, essayez de réduire")
    private String benefits;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Le code couleur doit être au format hexadécimal (#RRGGBB)")
    private String colorCode;
}
