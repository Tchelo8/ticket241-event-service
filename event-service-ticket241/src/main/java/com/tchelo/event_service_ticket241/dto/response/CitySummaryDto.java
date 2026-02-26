package com.tchelo.event_service_ticket241.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CitySummaryDto {

    private Long id;
    private String uuid;
    private String name;
    private String country;
    private String countryCode;
    private Integer eventCount;
}
