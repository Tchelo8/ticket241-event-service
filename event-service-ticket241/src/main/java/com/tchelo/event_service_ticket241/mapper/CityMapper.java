package com.tchelo.event_service_ticket241.mapper;

import com.tchelo.event_service_ticket241.dto.request.CreateCityDto;
import com.tchelo.event_service_ticket241.dto.request.UpdateCityDto;
import com.tchelo.event_service_ticket241.dto.response.CityResponseDto;
import com.tchelo.event_service_ticket241.dto.response.CitySummaryDto;
import com.tchelo.event_service_ticket241.entity.City;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class CityMapper {


    public City toEntity(CreateCityDto dto) {
        City city = new City();

        BeanUtils.copyProperties(dto, city);

        // Initialisation des champs
        city.setIsActive(true);
        city.setEventCount(0);

        return city;
    }


    public void updateEntity(City city, UpdateCityDto dto) {
        BeanUtils.copyProperties(dto, city, getNullPropertyNames(dto));
    }

    public CityResponseDto toResponseDto(City city) {
        CityResponseDto dto = new CityResponseDto();

        BeanUtils.copyProperties(city, dto);

        return dto;
    }


    public CitySummaryDto toSummaryDto(City city) {
        CitySummaryDto dto = new CitySummaryDto();

        BeanUtils.copyProperties(city, dto);

        return dto;
    }

    // utilitaire : Retourne les noms des propriétés null
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
