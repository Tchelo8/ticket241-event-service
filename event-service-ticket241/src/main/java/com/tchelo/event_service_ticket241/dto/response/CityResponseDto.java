package com.tchelo.event_service_ticket241.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CityResponseDto {

    private Long id;
    private String uuid;
    private String name;
    private String country;
    private String countryCode;
    private String region;
    private String postalCode;
    private Double latitude;
    private Double longitude;
    private Boolean isActive;
    private Integer eventCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
