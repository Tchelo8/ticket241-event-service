package com.tchelo.event_service_ticket241.service.impl;

import com.tchelo.event_service_ticket241.dto.request.StructureRequestDTO;
import com.tchelo.event_service_ticket241.dto.response.InvitationCodeCheckResponseDto;
import com.tchelo.event_service_ticket241.dto.response.InvitationCodeResponseDto;
import com.tchelo.event_service_ticket241.dto.response.StructureResponseDTO;
import com.tchelo.event_service_ticket241.entity.Structure;
import com.tchelo.event_service_ticket241.exception.InvalidEventDataException;
import com.tchelo.event_service_ticket241.exception.StructureAlreadyExistsException;
import com.tchelo.event_service_ticket241.exception.StructureNotFoundException;
import com.tchelo.event_service_ticket241.exception.UnauthorizedAccesException;
import com.tchelo.event_service_ticket241.mapper.StructureMapper;
import com.tchelo.event_service_ticket241.repository.EventRepository;
import com.tchelo.event_service_ticket241.repository.StructureRepository;
import com.tchelo.event_service_ticket241.security.UserContext;
import com.tchelo.event_service_ticket241.service.StructureService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class StructureServiceImpl implements StructureService {

    // Code lisible et dictable au téléphone : 12 caractères sans 0/O/1/I, groupés par 4 (≈ 60 bits)
    private static final String INVITATION_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int INVITATION_CODE_LENGTH = 12;
    private static final int INVITATION_VALIDITY_DAYS = 7;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final StructureRepository repository;
    private final EventRepository eventRepository;
    private final StructureMapper mapper;

    public StructureServiceImpl(StructureRepository repository, EventRepository eventRepository, StructureMapper mapper) {
        this.repository = repository;
        this.eventRepository = eventRepository;
        this.mapper = mapper;
    }

    @Override
    public StructureResponseDTO createStructure(StructureRequestDTO dto, UserContext user) {
        requireAdmin(user, "créer une structure");

        if (dto.getEmail() != null && repository.existsByEmail(dto.getEmail())) {
            throw new StructureAlreadyExistsException("Une structure avec cet email existe déjà");
        }
        Structure structure = mapper.requestDTOToEntity(dto);
        structure.setStatus(false);
        structure.setIsVerified(false);
        return mapper.entityToResponseDTO(repository.save(structure));
    }

    @Override
    public StructureResponseDTO getStructureById(Long id) {
        return mapper.entityToResponseDTO(findStructure(id));
    }

    @Override
    public StructureResponseDTO getStructureByUuid(String uuid) {
        Structure structure = repository.findByUuid(uuid);
        if (structure == null) {
            throw new StructureNotFoundException("Structure introuvable avec l'uuid : " + uuid);
        }
        return mapper.entityToResponseDTO(structure);
    }

    @Override
    public List<StructureResponseDTO> getAllStructures() {
        return repository.findAll()
                .stream()
                .map(mapper::entityToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public StructureResponseDTO updateStructure(Long id, StructureRequestDTO dto, UserContext user) {
        if (!user.canManageStructure(id)) {
            throw new UnauthorizedAccesException("Seuls les administrateurs de cette structure peuvent la modifier");
        }
        Structure structure = findStructure(id);

        if (dto.getEmail() != null && !dto.getEmail().equalsIgnoreCase(structure.getEmail())
                && repository.existsByEmail(dto.getEmail())) {
            throw new StructureAlreadyExistsException("Une structure avec cet email existe déjà");
        }

        // updateEntityFromDTO ne touche ni au statut, ni à la vérification, ni au code d'invitation
        mapper.updateEntityFromDTO(dto, structure);
        return mapper.entityToResponseDTO(repository.save(structure));
    }

    @Override
    public StructureResponseDTO verifyStructure(Long id, UserContext user) {
        requireAdmin(user, "vérifier une structure");

        Structure structure = findStructure(id);
        structure.setIsVerified(true);
        structure.setStatus(true);
        log.info("Structure {} vérifiée par {}", id, user.userId());
        return mapper.entityToResponseDTO(repository.save(structure));
    }

    @Override
    public void deleteStructure(Long id, UserContext user) {
        requireAdmin(user, "supprimer une structure");

        if (!repository.existsById(id)) {
            throw new StructureNotFoundException("Structure introuvable avec l'id : " + id);
        }
        if (eventRepository.existsByStructureId(id)) {
            throw new InvalidEventDataException(
                    "Impossible de supprimer une structure qui a des événements : désactivez-la plutôt");
        }
        repository.deleteById(id);
        log.info("Structure {} supprimée par {}", id, user.userId());
    }

    @Override
    public InvitationCodeResponseDto generateInvitationCode(Long id, UserContext user) {
        requireAdmin(user, "générer un code d'invitation");

        Structure structure = findStructure(id);
        String code = newInvitationCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(INVITATION_VALIDITY_DAYS);

        // Un seul code actif par structure : en générer un nouveau invalide le précédent
        structure.setInvitationCodeHash(hash(code));
        structure.setInvitationCodeExpiresAt(expiresAt);
        repository.save(structure);

        log.info("Code d'invitation généré pour la structure {} par {} (expire le {})", id, user.userId(), expiresAt);

        return InvitationCodeResponseDto.builder()
                .structureId(structure.getId())
                .structureName(structure.getNom())
                .invitationCode(code)
                .expiresAt(expiresAt)
                .message("Transmettez ce code à l'administrateur de la structure. Il n'est affiché qu'une fois, "
                        + "est valable " + INVITATION_VALIDITY_DAYS + " jours et ne sert qu'à une seule inscription.")
                .build();
    }

    @Override
    public InvitationCodeCheckResponseDto checkInvitationCode(Long id, String code) {
        Structure structure = repository.findById(id).orElse(null);
        return evaluate(structure, code);
    }

    @Override
    public InvitationCodeCheckResponseDto consumeInvitationCode(Long id, String code) {
        Structure structure = repository.findByIdForUpdate(id).orElse(null);
        InvitationCodeCheckResponseDto result = evaluate(structure, code);

        if (result.isValid()) {
            // Usage unique : le code ne pourra plus servir
            structure.setInvitationCodeHash(null);
            structure.setInvitationCodeExpiresAt(null);
            repository.save(structure);
            log.info("Code d'invitation de la structure {} utilisé", id);
        }
        return result;
    }

    // Même message pour un code faux, expiré ou une structure inconnue : on ne renseigne pas un attaquant
    private InvitationCodeCheckResponseDto evaluate(Structure structure, String code) {
        if (structure == null || structure.getInvitationCodeHash() == null || code == null) {
            return new InvitationCodeCheckResponseDto(false, "Code d'invitation invalide ou expiré");
        }
        if (structure.getInvitationCodeExpiresAt() == null
                || structure.getInvitationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            return new InvitationCodeCheckResponseDto(false, "Code d'invitation invalide ou expiré");
        }
        boolean matches = MessageDigest.isEqual(
                hash(code).getBytes(StandardCharsets.UTF_8),
                structure.getInvitationCodeHash().getBytes(StandardCharsets.UTF_8));
        return matches
                ? new InvitationCodeCheckResponseDto(true, "Code d'invitation valide")
                : new InvitationCodeCheckResponseDto(false, "Code d'invitation invalide ou expiré");
    }

    private Structure findStructure(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new StructureNotFoundException("Structure introuvable avec l'id : " + id));
    }

    private void requireAdmin(UserContext user, String action) {
        if (!user.isAdmin()) {
            throw new UnauthorizedAccesException("Seul un administrateur Ticket241 peut " + action);
        }
    }

    // Format XXXX-XXXX-XXXX
    private String newInvitationCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < INVITATION_CODE_LENGTH; i++) {
            if (i > 0 && i % 4 == 0) {
                code.append('-');
            }
            code.append(INVITATION_ALPHABET.charAt(SECURE_RANDOM.nextInt(INVITATION_ALPHABET.length())));
        }
        return code.toString();
    }

    // Insensible à la casse, aux espaces et aux tirets : "abcd efgh-jklm" = "ABCD-EFGH-JKLM"
    private String hash(String code) {
        String normalized = code.toUpperCase(Locale.ROOT).replaceAll("[\\s-]", "");
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(normalized.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }
}
