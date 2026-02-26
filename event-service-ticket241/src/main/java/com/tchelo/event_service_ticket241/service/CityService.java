package com.tchelo.event_service_ticket241.service;

import com.tchelo.event_service_ticket241.dto.request.CreateCityDto;
import com.tchelo.event_service_ticket241.dto.request.UpdateCityDto;
import com.tchelo.event_service_ticket241.dto.response.CityResponseDto;
import com.tchelo.event_service_ticket241.dto.response.CitySummaryDto;

import java.util.List;

public interface CityService {


    CityResponseDto createCity(CreateCityDto dto, String userId, String roles);


    CityResponseDto updateCity(String uuid, UpdateCityDto dto, String userId, String roles);


    CityResponseDto getCityByUuid(String uuid);


    CityResponseDto getCityById(Long id);


    List<CitySummaryDto> getAllActiveCities();


    List<CitySummaryDto> getCitiesByCountry(String country);


    List<CitySummaryDto> searchCities(String keyword);


    void deleteCity(String uuid, String userId, String roles);


    CityResponseDto toggleCityStatus(String uuid, String userId, String roles);
}
