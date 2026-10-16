package com.tchelo.event_service_ticket241.service;

import com.tchelo.event_service_ticket241.dto.response.PublicStructureDto;

import java.util.List;

// Annuaire public des organisateurs (sans compte) : structures vérifiées, fiche réduite
public interface PublicStructureService {

    List<PublicStructureDto> getVerifiedStructures();
}
