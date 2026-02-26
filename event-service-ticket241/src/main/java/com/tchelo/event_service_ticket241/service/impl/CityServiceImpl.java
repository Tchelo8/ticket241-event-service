package com.tchelo.event_service_ticket241.service.impl;

import com.tchelo.event_service_ticket241.dto.request.CreateCityDto;
import com.tchelo.event_service_ticket241.dto.request.UpdateCityDto;
import com.tchelo.event_service_ticket241.dto.response.CityResponseDto;
import com.tchelo.event_service_ticket241.dto.response.CitySummaryDto;
import com.tchelo.event_service_ticket241.entity.City;
import com.tchelo.event_service_ticket241.exception.CityNotFoundException;
import com.tchelo.event_service_ticket241.exception.InvalidEventDataException;
import com.tchelo.event_service_ticket241.exception.UnauthorizedAccesException;
import com.tchelo.event_service_ticket241.mapper.CityMapper;
import com.tchelo.event_service_ticket241.repository.CityRepository;
import com.tchelo.event_service_ticket241.service.CityService;
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
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;
    private final CityMapper cityMapper;

    @Override
    public CityResponseDto createCity(CreateCityDto dto, String userId, String roles) {
        log.info("Création d'une ville par l'utilisateur : {}", userId);

        // Vérifier que l'utilisateur qui crée la ville est un admin
        if (!roles.contains("ROLE_ADMIN")) {
            throw new UnauthorizedAccesException(
                    "Seuls les administrateurs peuvent créer des villes sur Ticket241 "
            );
        }

        // Vérifier que la ville n'existe pas déjà
        if (cityRepository.existsByNameIgnoreCaseAndCountryIgnoreCase(dto.getName(), dto.getCountry())) {
            throw new InvalidEventDataException(
                    String.format("La ville %s (%s) existe déjà dans la plateforme", dto.getName(), dto.getCountry())
            );
        }

        // Créer l'entité
        City city = cityMapper.toEntity(dto);

        // Sauvegarder
        City savedCity = cityRepository.save(city);

        log.info("✅ Ville créée avec succès : {} - {}", savedCity.getName(), savedCity.getCountry());

        return cityMapper.toResponseDto(savedCity);
    }

    @Override
    public CityResponseDto updateCity(String uuid, UpdateCityDto dto, String userId, String roles) {
        log.info("Mise à jour de la ville {} par l'utilisateur {}", uuid, userId);

        // Vérifier que l'utilisateur est admin
        if (!roles.contains("ROLE_ADMIN")) {
            throw new UnauthorizedAccesException(
                    "Seuls les administrateurs peuvent modifier des villes"
            );
        }

        // Récupérer la ville
        City city = cityRepository.findByUuid(uuid)
                .orElseThrow(() -> new CityNotFoundException(
                        "Ville non trouvée avec l'UUID : " + uuid
                ));

        // Vérifier que le nouveau nom n'existe pas déjà (si changé)
        if (dto.getName() != null && !dto.getName().equalsIgnoreCase(city.getName())) {
            String country = dto.getCountry() != null ? dto.getCountry() : city.getCountry();

            if (cityRepository.existsByNameIgnoreCaseAndCountryIgnoreCase(dto.getName(), country)) {
                throw new InvalidEventDataException(
                        String.format("La ville %s (%s) existe déjà sur Ticket241 ", dto.getName(), country)
                );
            }
        }

        // Mettre à jour
        cityMapper.updateEntity(city, dto);

        // Sauvegarder
        City updatedCity = cityRepository.save(city);

        log.info("Ville mise à jour avec succès : {}", uuid);

        return cityMapper.toResponseDto(updatedCity);
    }

    @Override
    @Transactional(readOnly = true)
    public CityResponseDto getCityByUuid(String uuid) {
        City city = cityRepository.findByUuid(uuid)
                .orElseThrow(() -> new CityNotFoundException(
                        "Ville non trouvée sur Ticket241 avec l'UUID : " + uuid
                ));

        return cityMapper.toResponseDto(city);
    }

    @Override
    @Transactional(readOnly = true)
    public CityResponseDto getCityById(Long id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new CityNotFoundException(
                        "Ville non trouvée sur Ticket241 avec l'ID : " + id
                ));

        return cityMapper.toResponseDto(city);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitySummaryDto> getAllActiveCities() {
        return cityRepository.findByIsActiveTrueOrderByNameAsc()
                .stream()
                .map(cityMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitySummaryDto> getCitiesByCountry(String country) {
        return cityRepository.findByCountryIgnoreCaseAndIsActiveTrueOrderByNameAsc(country)
                .stream()
                .map(cityMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitySummaryDto> searchCities(String keyword) {
        return cityRepository.searchCities(keyword)
                .stream()
                .map(cityMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCity(String uuid, String userId, String roles) {
        log.info("Suppression de la ville {} par l'utilisateur {}", uuid, userId);

        // Vérifier que l'utilisateur est admin
        if (!roles.contains("ROLE_ADMIN")) {
            throw new UnauthorizedAccesException(
                    "Seuls les administrateurs peuvent supprimer des villes sur Ticket241"
            );
        }

        City city = cityRepository.findByUuid(uuid)
                .orElseThrow(() -> new CityNotFoundException(
                        "Ville non trouvée sur Ticket241 avec l'UUID : " + uuid
                ));

        // Vérification qu'il n'y a pas d'événements actifs dans cette ville
        if (city.getEventCount() > 0) {
            throw new InvalidEventDataException(
                    String.format("Impossible de supprimer la ville %s car elle contient %d événement(s) actif(s)",
                            city.getName(), city.getEventCount())
            );
        }

        // Soft delete
        city.setIsActive(false);
        cityRepository.save(city);

        log.info("Ville supprimée (soft delete) : {}", uuid);
    }

    @Override
    public CityResponseDto toggleCityStatus(String uuid, String userId, String roles) {
        log.info("Changement de statut de la ville {} par l'utilisateur {}", uuid, userId);

        // Vérifier que l'utilisateur est admin
        if (!roles.contains("ROLE_ADMIN")) {
            throw new UnauthorizedAccesException(
                    "Seuls les administrateurs peuvent changer le statut des villes sur Ticket241"
            );
        }

        City city = cityRepository.findByUuid(uuid)
                .orElseThrow(() -> new CityNotFoundException(
                        "Ville non trouvée sur Ticket241 avec l'UUID : " + uuid
                ));

        // Inverser le statut
        city.setIsActive(!city.getIsActive());

        City updatedCity = cityRepository.save(city);

        log.info("Statut de la ville changé : {} - Actif: {}", city.getName(), city.getIsActive());

        return cityMapper.toResponseDto(updatedCity);
    }
}
