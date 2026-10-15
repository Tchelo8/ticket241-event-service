package com.tchelo.event_service_ticket241.mapper;

import com.tchelo.event_service_ticket241.dto.request.CreateCategoryDto;
import com.tchelo.event_service_ticket241.dto.request.UpdateCategoryDto;
import com.tchelo.event_service_ticket241.dto.response.CategoryResponseDto;
import com.tchelo.event_service_ticket241.dto.response.CategorySummaryDto;
import com.tchelo.event_service_ticket241.entity.Category;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toEntity(CreateCategoryDto dto) {
        Category category = new Category();
        BeanUtils.copyProperties(dto, category);
        category.setIsActive(true);
        return category;
    }

    public void updateEntity(Category category, UpdateCategoryDto dto) {
        BeanUtils.copyProperties(dto, category, getNullPropertyNames(dto));
    }

    public CategoryResponseDto toResponseDto(Category category) {
        CategoryResponseDto dto = new CategoryResponseDto();
        BeanUtils.copyProperties(category, dto);
        return dto;
    }

    public CategorySummaryDto toSummaryDto(Category category) {
        CategorySummaryDto dto = new CategorySummaryDto();
        BeanUtils.copyProperties(category, dto);
        return dto;
    }

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
