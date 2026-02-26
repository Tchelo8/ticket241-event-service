package com.tchelo.event_service_ticket241.mapper;

import com.tchelo.event_service_ticket241.dto.request.CreateTicketCategoryDto;
import com.tchelo.event_service_ticket241.dto.response.TicketCategoryResponseDto;
import com.tchelo.event_service_ticket241.entity.TicketCategory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class TicketCategoryMapper {

    public TicketCategory toEntity(CreateTicketCategoryDto dto) {
        TicketCategory ticketCategory = new TicketCategory();

        BeanUtils.copyProperties(dto, ticketCategory);

        // Initialisation des sièges
        ticketCategory.setAvailableSeats(dto.getTotalSeats());
        ticketCategory.setReservedSeats(0);
        ticketCategory.setSoldSeats(0);
        ticketCategory.setIsActive(true);

        return ticketCategory;
    }



    public TicketCategoryResponseDto toResponseDto(TicketCategory ticketCategory) {
        TicketCategoryResponseDto dto = new TicketCategoryResponseDto();

        BeanUtils.copyProperties(ticketCategory, dto);

        // Calcul des informations dérivées concernant les places dispo
        dto.setHasAvailableSeats(ticketCategory.hasAvailableSeats(1));

        return dto;
    }
}
