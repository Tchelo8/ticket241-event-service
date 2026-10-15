package com.tchelo.event_service_ticket241.controller;

import com.tchelo.event_service_ticket241.dto.request.CreateCategoryDto;
import com.tchelo.event_service_ticket241.dto.request.UpdateCategoryDto;
import com.tchelo.event_service_ticket241.dto.response.CategoryResponseDto;
import com.tchelo.event_service_ticket241.dto.response.CategorySummaryDto;
import com.tchelo.event_service_ticket241.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;


    // ============================================================
    // Endpoint Public — accessible sans authentification
    // ============================================================

    @GetMapping("/get/all")
    public ResponseEntity<List<CategorySummaryDto>> getAllActiveCategories() {
        log.info("Récupération de toutes les catégories actives sur Ticket241");

        List<CategorySummaryDto> categories = categoryService.getAllActiveCategories();

        log.info("{} catégories récupérées", categories.size());

        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<CategoryResponseDto> getCategoryByUuid(@PathVariable String uuid) {
        log.info("Récupération de la catégorie : {}", uuid);

        CategoryResponseDto category = categoryService.getCategoryByUuid(uuid);

        return ResponseEntity.ok(category);
    }


    // ============================================================
    // Endpoints Protégés — réservés aux administrateurs
    // ============================================================

    @PostMapping
    public ResponseEntity<CategoryResponseDto> createCategory(
            @Valid @RequestBody CreateCategoryDto dto,
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestHeader(value = "X-User-Roles", required = true) String roles
    ) {
        log.info("Création d'une catégorie par l'utilisateur : {}", userId);

        CategoryResponseDto createdCategory = categoryService.createCategory(dto, userId, roles);

        log.info("Catégorie créée avec succès : {}", createdCategory.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<CategoryResponseDto> updateCategory(
            @PathVariable String uuid,
            @Valid @RequestBody UpdateCategoryDto dto,
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestHeader(value = "X-User-Roles", required = true) String roles
    ) {
        log.info("Mise à jour de la catégorie {} par l'utilisateur {}", uuid, userId);

        CategoryResponseDto updatedCategory = categoryService.updateCategory(uuid, dto, userId, roles);

        log.info("Catégorie mise à jour avec succès : {}", uuid);

        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/delete/{uuid}")
    public ResponseEntity<Map<String, String>> deleteCategory(
            @PathVariable String uuid,
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestHeader(value = "X-User-Roles", required = true) String roles
    ) {
        log.info("Suppression de la catégorie {} par l'utilisateur {}", uuid, userId);

        categoryService.deleteCategory(uuid, userId, roles);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Catégorie supprimée avec succès de la plateforme Ticket241");
        response.put("uuid", uuid);

        log.info("Catégorie supprimée avec succès : {}", uuid);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{uuid}/toggle-status")
    public ResponseEntity<CategoryResponseDto> toggleCategoryStatus(
            @PathVariable String uuid,
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestHeader(value = "X-User-Roles", required = true) String roles
    ) {
        log.info("Changement de statut de la catégorie {} par l'utilisateur {}", uuid, userId);

        CategoryResponseDto updatedCategory = categoryService.toggleCategoryStatus(uuid, userId, roles);

        log.info("Statut de la catégorie changé : {}", uuid);

        return ResponseEntity.ok(updatedCategory);
    }
}
