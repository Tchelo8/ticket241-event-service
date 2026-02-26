package com.tchelo.event_service_ticket241.mapper;

import com.tchelo.event_service_ticket241.dto.request.CreateEventDto;
import com.tchelo.event_service_ticket241.dto.request.UpdateEventDto;
import com.tchelo.event_service_ticket241.dto.response.EventResponseDto;
import com.tchelo.event_service_ticket241.dto.response.EventSummaryDto;
import com.tchelo.event_service_ticket241.entity.City;
import com.tchelo.event_service_ticket241.entity.Event;
import com.tchelo.event_service_ticket241.entity.Structure;
import com.tchelo.event_service_ticket241.entity.enums.EventStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EventMapper {

    private final TicketCategoryMapper ticketCategoryMapper;

    //Convertit CreateEventDto en Event (sans les catégories de tickets, les catégories sont ajoutées dans le service
    public Event toEntity(CreateEventDto dto, Structure structure, City city) {
        Event event = new Event();
        BeanUtils.copyProperties(dto, event, "ticketCategories", "structureId", "cityId");

        // Assignation manuelle des champs spécifiques
        event.setStructure(structure);
        event.setCity(city);
        event.setEventStatus(EventStatus.DRAFT);  // Statut de départ d"un evènement
        event.setAvailableSeats(dto.getTotalSeats());
        event.setReservedSeats(0);
        event.setSoldSeats(0);
        event.setSlug(generateSlug(dto.getName()));

        return event;
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[àáâãäå]", "a")
                .replaceAll("[èéêë]", "e")
                .replaceAll("[ìíîï]", "i")
                .replaceAll("[òóôõö]", "o")
                .replaceAll("[ùúûü]", "u")
                .replaceAll("[ç]", "c")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim()
                + "-" + System.currentTimeMillis();
    }

    //Met à jour une entité Event avec les données d'UpdateEventDto utilise BeanUtils avec ignoreNullValues pour ne mettre à jour que les champs non-null
    public void updateEntity(Event event, UpdateEventDto dto) {

        BeanUtils.copyProperties(dto, event, getNullPropertyNames(dto));
    }

    //Convertit Event en EventResponseDto (complet avec catégories)
    public EventResponseDto toResponseDto(Event event) {
        EventResponseDto dto = new EventResponseDto();

        BeanUtils.copyProperties(event, dto, "structure", "ticketCategories");

        if (event.getStructure() != null) {
            dto.setStructureId(event.getStructure().getId());
            dto.setStructureName(event.getStructure().getNom());
        }

        if (event.getCity() != null) {
            dto.setCityId(event.getCity().getId());
            dto.setCityName(event.getCity().getName());
            dto.setCityCountry(event.getCity().getCountry());
        }

        // Conversion des catégories de tickets
        if (event.getTicketCategories() != null) {
            dto.setTicketCategories(
                    event.getTicketCategories().stream()
                            .map(ticketCategoryMapper::toResponseDto)
                            .collect(Collectors.toList())
            );
        }

        dto.setIsSaleOpen(event.isSaleOpen());
        dto.setIsPastEvent(event.isPastEvent());
        dto.setIsSoldOut(event.getAvailableSeats() != null && event.getAvailableSeats() == 0);

        return dto;
    }

    // Convertit Event en EventSummaryDto (version légère pour les listes)
    public EventSummaryDto toSummaryDto(Event event) {
        EventSummaryDto dto = new EventSummaryDto();

        BeanUtils.copyProperties(event, dto);

        if (event.getCity() != null) {
            dto.setCityId(event.getCity().getId());
            dto.setCityName(event.getCity().getName());
            dto.setCityCountry(event.getCity().getCountry());
        }

        dto.setIsSaleOpen(event.isSaleOpen());
        dto.setIsPastEvent(event.isPastEvent());
        dto.setIsSoldOut(event.getAvailableSeats() != null && event.getAvailableSeats() == 0);

        return dto;
    }

    // Utilitaire qui retournera les noms des propriétés null d'un objet utilisé pour ignorer les champs null lors de la mise à jour

    private String[] getNullPropertyNames(Object source) {
        final org.springframework.beans.BeanWrapper src = new org.springframework.beans.BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        java.util.Set<String> emptyNames = new java.util.HashSet<>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }

        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }
}
