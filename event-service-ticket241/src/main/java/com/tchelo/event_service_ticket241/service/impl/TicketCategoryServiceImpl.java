package com.tchelo.event_service_ticket241.service.impl;

import com.tchelo.event_service_ticket241.dto.response.TicketCategoryResponseDto;
import com.tchelo.event_service_ticket241.entity.TicketCategory;
import com.tchelo.event_service_ticket241.exception.InsufficientSeatsException;
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
@Transactional
public class TicketCategoryServiceImpl implements TicketCategoryService {

    private final TicketCategoryRepository ticketCategoryRepository;
    private final TicketCategoryMapper ticketCategoryMapper;

    @Override
    @Transactional(readOnly = true)
    public TicketCategoryResponseDto getCategoryById(Long categoryId) {
        TicketCategory category = ticketCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new TicketCategoryNotFoundException(
                        "Catégorie de ticket non trouvée avec l'ID : " + categoryId
                ));

        return ticketCategoryMapper.toResponseDto(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean checkAvailability(Long categoryId, Integer quantity) {
        TicketCategory category = ticketCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new TicketCategoryNotFoundException(
                        "Catégorie de ticket non trouvée avec l'ID : " + categoryId
                ));

        boolean isAvailable = category.hasAvailableSeats(quantity);

        log.info("Vérification disponibilité catégorie {} : {} places demandées, {} disponibles → {}",
                categoryId, quantity, category.getAvailableSeats(), isAvailable);

        return isAvailable;
    }

    @Override
    public void decreaseAvailableSeats(Long categoryId, Integer quantity) {
        TicketCategory category = ticketCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new TicketCategoryNotFoundException(
                        "Catégorie de ticket non trouvée avec l'ID : " + categoryId
                ));

        if (!category.hasAvailableSeats(quantity)) {
            throw new InsufficientSeatsException(
                    String.format("Places insuffisantes. Disponibles: %d, Demandées: %d",
                            category.getAvailableSeats(), quantity)
            );
        }

        category.setAvailableSeats(category.getAvailableSeats() - quantity);
        category.setSoldSeats(category.getSoldSeats() + quantity);

        ticketCategoryRepository.save(category);

        log.info("{} places décrémentées pour catégorie {}", quantity, categoryId);
    }

    @Override
    public void increaseAvailableSeats(Long categoryId, Integer quantity) {
        TicketCategory category = ticketCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new TicketCategoryNotFoundException(
                        "Catégorie de ticket non trouvée avec l'ID : " + categoryId
                ));

        category.setAvailableSeats(category.getAvailableSeats() + quantity);

        if (category.getSoldSeats() >= quantity) {
            category.setSoldSeats(category.getSoldSeats() - quantity);
        }

        ticketCategoryRepository.save(category);

        log.info("{} places libérées pour catégorie {}", quantity, categoryId);
    }

    @Override
    public void reserveSeats(Long categoryId, Integer quantity) {
        TicketCategory category = ticketCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new TicketCategoryNotFoundException(
                        "Catégorie de ticket non trouvée avec l'ID : " + categoryId
                ));

        category.reserveSeats(quantity);

        ticketCategoryRepository.save(category);

        log.info("{} places réservées pour catégorie {}", quantity, categoryId);
    }

    @Override
    public void cancelReservation(Long categoryId, Integer quantity) {
        TicketCategory category = ticketCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new TicketCategoryNotFoundException(
                        "Catégorie de ticket non trouvée avec l'ID : " + categoryId
                ));

        category.cancelReservation(quantity);

        ticketCategoryRepository.save(category);

        log.info("Réservation de {} places annulée pour catégorie {}", quantity, categoryId);
    }
}
