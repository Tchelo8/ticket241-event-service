package com.tchelo.event_service_ticket241.service;

import com.tchelo.event_service_ticket241.dto.request.CreateCategoryDto;
import com.tchelo.event_service_ticket241.dto.request.UpdateCategoryDto;
import com.tchelo.event_service_ticket241.dto.response.CategoryResponseDto;
import com.tchelo.event_service_ticket241.dto.response.CategorySummaryDto;

import java.util.List;

public interface CategoryService {

    CategoryResponseDto createCategory(CreateCategoryDto dto, String userId, String roles);

    CategoryResponseDto updateCategory(String uuid, UpdateCategoryDto dto, String userId, String roles);

    CategoryResponseDto getCategoryByUuid(String uuid);

    CategoryResponseDto getCategoryById(Long id);

    List<CategorySummaryDto> getAllActiveCategories();

    void deleteCategory(String uuid, String userId, String roles);

    CategoryResponseDto toggleCategoryStatus(String uuid, String userId, String roles);
}
