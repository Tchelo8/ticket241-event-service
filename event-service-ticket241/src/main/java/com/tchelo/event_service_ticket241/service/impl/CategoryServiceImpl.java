package com.tchelo.event_service_ticket241.service.impl;

import com.tchelo.event_service_ticket241.dto.request.CreateCategoryDto;
import com.tchelo.event_service_ticket241.dto.request.UpdateCategoryDto;
import com.tchelo.event_service_ticket241.dto.response.CategoryResponseDto;
import com.tchelo.event_service_ticket241.dto.response.CategorySummaryDto;
import com.tchelo.event_service_ticket241.entity.Category;
import com.tchelo.event_service_ticket241.exception.CategoryNotFoundException;
import com.tchelo.event_service_ticket241.exception.InvalidEventDataException;
import com.tchelo.event_service_ticket241.exception.UnauthorizedAccesException;
import com.tchelo.event_service_ticket241.mapper.CategoryMapper;
import com.tchelo.event_service_ticket241.repository.CategoryRepository;
import com.tchelo.event_service_ticket241.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponseDto createCategory(CreateCategoryDto dto, String userId, String roles) {
        log.info("Création d'une catégorie par l'utilisateur : {}", userId);

        // Seuls les admins peuvent créer des catégories
        if (!roles.contains("ROLE_ADMIN")) {
            throw new UnauthorizedAccesException(
                    "Seuls les administrateurs peuvent créer des catégories sur Ticket241"
            );
        }

        // Vérifier l'unicité du nom
        if (categoryRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new InvalidEventDataException(
                    String.format("La catégorie '%s' existe déjà sur la plateforme", dto.getName())
            );
        }

        Category category = categoryMapper.toEntity(dto);
        Category savedCategory = categoryRepository.save(category);

        log.info("Catégorie créée avec succès : {}", savedCategory.getName());

        return categoryMapper.toResponseDto(savedCategory);
    }

    @Override
    public CategoryResponseDto updateCategory(String uuid, UpdateCategoryDto dto, String userId, String roles) {
        log.info("Mise à jour de la catégorie {} par l'utilisateur {}", uuid, userId);

        if (!roles.contains("ROLE_ADMIN")) {
            throw new UnauthorizedAccesException(
                    "Seuls les administrateurs peuvent modifier des catégories sur Ticket241"
            );
        }

        Category category = categoryRepository.findByUuid(uuid)
                .orElseThrow(() -> new CategoryNotFoundException(
                        "Catégorie non trouvée avec l'UUID : " + uuid
                ));

        // Vérifier que le nouveau nom n'existe pas déjà (si changé)
        if (dto.getName() != null && !dto.getName().equalsIgnoreCase(category.getName())) {
            if (categoryRepository.existsByNameIgnoreCase(dto.getName())) {
                throw new InvalidEventDataException(
                        String.format("La catégorie '%s' existe déjà sur Ticket241", dto.getName())
                );
            }
        }

        categoryMapper.updateEntity(category, dto);
        Category updatedCategory = categoryRepository.save(category);

        log.info("Catégorie mise à jour avec succès : {}", uuid);

        return categoryMapper.toResponseDto(updatedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDto getCategoryByUuid(String uuid) {
        Category category = categoryRepository.findByUuid(uuid)
                .orElseThrow(() -> new CategoryNotFoundException(
                        "Catégorie non trouvée sur Ticket241 avec l'UUID : " + uuid
                ));

        return categoryMapper.toResponseDto(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(
                        "Catégorie non trouvée sur Ticket241 avec l'ID : " + id
                ));

        return categoryMapper.toResponseDto(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategorySummaryDto> getAllActiveCategories() {
        return categoryRepository.findByIsActiveTrueOrderByNameAsc()
                .stream()
                .map(categoryMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCategory(String uuid, String userId, String roles) {
        log.info("Suppression de la catégorie {} par l'utilisateur {}", uuid, userId);

        if (!roles.contains("ROLE_ADMIN")) {
            throw new UnauthorizedAccesException(
                    "Seuls les administrateurs peuvent supprimer des catégories sur Ticket241"
            );
        }

        Category category = categoryRepository.findByUuid(uuid)
                .orElseThrow(() -> new CategoryNotFoundException(
                        "Catégorie non trouvée sur Ticket241 avec l'UUID : " + uuid
                ));

        // Soft delete
        category.setIsActive(false);
        categoryRepository.save(category);

        log.info("Catégorie supprimée (soft delete) : {}", uuid);
    }

    @Override
    public CategoryResponseDto toggleCategoryStatus(String uuid, String userId, String roles) {
        log.info("Changement de statut de la catégorie {} par l'utilisateur {}", uuid, userId);

        if (!roles.contains("ROLE_ADMIN")) {
            throw new UnauthorizedAccesException(
                    "Seuls les administrateurs peuvent changer le statut des catégories sur Ticket241"
            );
        }

        Category category = categoryRepository.findByUuid(uuid)
                .orElseThrow(() -> new CategoryNotFoundException(
                        "Catégorie non trouvée sur Ticket241 avec l'UUID : " + uuid
                ));

        category.setIsActive(!category.getIsActive());
        Category updatedCategory = categoryRepository.save(category);

        log.info("Statut de la catégorie changé : {} - Actif: {}", category.getName(), category.getIsActive());

        return categoryMapper.toResponseDto(updatedCategory);
    }
}
