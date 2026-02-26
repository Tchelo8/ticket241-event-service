package com.tchelo.event_service_ticket241.controller;

import com.tchelo.event_service_ticket241.dto.request.CreateCityDto;
import com.tchelo.event_service_ticket241.dto.request.UpdateCityDto;
import com.tchelo.event_service_ticket241.dto.response.CityResponseDto;
import com.tchelo.event_service_ticket241.dto.response.CitySummaryDto;
import com.tchelo.event_service_ticket241.service.CityService;
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
@RequestMapping("/api/events/cities")
@RequiredArgsConstructor
@Slf4j
public class CityController {

    private final CityService cityService;

    // Endpoints Publics (Accessibles sans authentification)

    @GetMapping("/get/all/active")
    public ResponseEntity<List<CitySummaryDto>> getAllActiveCities() {
        log.info("Récupération de toutes les villes actives");

        List<CitySummaryDto> cities = cityService.getAllActiveCities();

        log.info("{} villes récupérées", cities.size());

        return ResponseEntity.ok(cities);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<CityResponseDto> getCityByUuid(@PathVariable String uuid) {
        log.info("Récupération de la ville : {}", uuid);

        CityResponseDto city = cityService.getCityByUuid(uuid);

        return ResponseEntity.ok(city);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<CityResponseDto> getCityById(@PathVariable Long id) {
        log.info("Récupération de la ville ID : {}", id);

        CityResponseDto city = cityService.getCityById(id);

        return ResponseEntity.ok(city);
    }

    @GetMapping("/country/{country}")
    public ResponseEntity<List<CitySummaryDto>> getCitiesByCountry(@PathVariable String country) {
        log.info("Récupération des villes du pays : {}", country);

        List<CitySummaryDto> cities = cityService.getCitiesByCountry(country);

        log.info("{} villes trouvées pour {}", cities.size(), country);

        return ResponseEntity.ok(cities);
    }

    @GetMapping("/search")
    public ResponseEntity<List<CitySummaryDto>> searchCities(@RequestParam String keyword) {
        log.info("Recherche de villes avec le mot-clé : {}", keyword);

        List<CitySummaryDto> cities = cityService.searchCities(keyword);

        log.info("{} villes trouvées", cities.size());

        return ResponseEntity.ok(cities);
    }


    // Endpoints Protégés (Admin uniquement)

    @PostMapping
    public ResponseEntity<CityResponseDto> createCity(
            @Valid @RequestBody CreateCityDto dto,
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestHeader(value = "X-User-Roles", required = true) String roles
    ) {
        log.info("Création d'une ville par l'utilisateur : {}", userId);

        CityResponseDto createdCity = cityService.createCity(dto, userId, roles);

        log.info("Ville créée avec succès sur Ticket241: {}", createdCity.getUuid());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdCity);
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<CityResponseDto> updateCity(
            @PathVariable String uuid,
            @Valid @RequestBody UpdateCityDto dto,
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestHeader(value = "X-User-Roles", required = true) String roles
    ) {
        log.info("Mise à jour de la ville {} par l'utilisateur {}", uuid, userId);

        CityResponseDto updatedCity = cityService.updateCity(uuid, dto, userId, roles);

        log.info("Ville mise à jour avec succès sur Ticket241");

        return ResponseEntity.ok(updatedCity);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Map<String, String>> deleteCity(
            @PathVariable String uuid,
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestHeader(value = "X-User-Roles", required = true) String roles
    ) {
        log.info("Suppression de la ville {} par l'utilisateur {}", uuid, userId);

        cityService.deleteCity(uuid, userId, roles);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Ville supprimée avec succès");
        response.put("uuid", uuid);

        log.info("Ville supprimée avec succès");

        return ResponseEntity.ok(response);
    }


    @PatchMapping("/{uuid}/toggle-status")
    public ResponseEntity<CityResponseDto> toggleCityStatus(
            @PathVariable String uuid,
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestHeader(value = "X-User-Roles", required = true) String roles
    ) {
        log.info("Changement de statut de la ville {} par l'utilisateur {}", uuid, userId);

        CityResponseDto city = cityService.toggleCityStatus(uuid, userId, roles);

        log.info("Statut de la ville changé : Actif = {}", city.getIsActive());

        return ResponseEntity.ok(city);
    }
}
