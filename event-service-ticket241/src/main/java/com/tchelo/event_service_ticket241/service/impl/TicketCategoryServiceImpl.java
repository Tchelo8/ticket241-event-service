package com.tchelo.event_service_ticket241.service.impl;

import com.tchelo.event_service_ticket241.dto.response.TicketCategoryResponseDto;
import com.tchelo.event_service_ticket241.entity.TicketCategory;
import com.tchelo.event_service_ticket241.exception.TicketCategoryNotFoundException;
import com.tchelo.event_service_ticket241.mapper.TicketCategoryMapper;
import com.tchelo.event_service_ticket241.repository.TicketCategoryRepository;
import com.tchelo.event_service_ticket241.service.TicketCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TicketCategoryServiceImpl implements TicketCategoryService {

    private final TicketCategoryRepository ticketCategoryRepository;
    private final TicketCategoryMapper ticketCategoryMapper;

    @Override
    public TicketCategoryResponseDto getCategoryById(Long categoryId) {
        TicketCategory category = ticketCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new TicketCategoryNotFoundException(
                        "Catégorie de ticket non trouvée avec l'ID : " + categoryId
                ));

        return ticketCategoryMapper.toResponseDto(category);
    }

    @Override
    public TicketCategoryResponseDto getCategoryForEvent(Long eventId, Long categoryId) {
        return ticketCategoryMapper.toResponseDto(findCategoryOfEvent(eventId, categoryId));
    }

    @Override
    public Boolean checkAvailability(Long eventId, Long categoryId, Integer quantity) {
        TicketCategory category = findCategoryOfEvent(eventId, categoryId);

        boolean isAvailable = category.hasAvailableSeats(quantity);

        log.info("Vérification disponibilité catégorie {} : {} places demandées, {} disponibles → {}",
                categoryId, quantity, category.getAvailableSeats(), isAvailable);

        return isAvailable;
    }

    // Une catégorie qui existe mais appartient à un autre événement est traitée comme introuvable :
    // sinon on pourrait réserver le prix d'une catégorie d'un autre événement
    private TicketCategory findCategoryOfEvent(Long eventId, Long categoryId) {
        return ticketCategoryRepository.findById(categoryId)
                .filter(category -> category.getEvent() != null && category.getEvent().getId().equals(eventId))
                .orElseThrow(() -> new TicketCategoryNotFoundException(
                        "Catégorie de ticket " + categoryId + " introuvable pour l'événement " + eventId
                ));
    }
}
