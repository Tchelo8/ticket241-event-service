package com.tchelo.event_service_ticket241.service.impl;

import com.tchelo.event_service_ticket241.dto.request.StructureRequestDTO;
import com.tchelo.event_service_ticket241.dto.response.InvitationCodeResponseDto;
import com.tchelo.event_service_ticket241.entity.Structure;
import com.tchelo.event_service_ticket241.exception.InvalidEventDataException;
import com.tchelo.event_service_ticket241.exception.UnauthorizedAccesException;
import com.tchelo.event_service_ticket241.mapper.StructureMapper;
import com.tchelo.event_service_ticket241.repository.EventRepository;
import com.tchelo.event_service_ticket241.repository.StructureRepository;
import com.tchelo.event_service_ticket241.security.UserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StructureServiceImplTest {

    private static final UserContext ADMIN = UserContext.of("+24100000000", "ROLE_ADMIN", null);
    private static final UserContext USER = UserContext.of("+24100000001", "ROLE_USER", null);
    private static final UserContext ADMIN_OF_STRUCTURE_1 = UserContext.of("+24100000002", "ROLE_STRUCTURE_ADMIN", "1");
    private static final UserContext ADMIN_OF_STRUCTURE_2 = UserContext.of("+24100000003", "ROLE_STRUCTURE_ADMIN", "2");

    @Mock
    private StructureRepository structureRepository;
    @Mock
    private EventRepository eventRepository;

    private StructureServiceImpl service;
    private Structure structure;

    @BeforeEach
    void setUp() {
        service = new StructureServiceImpl(structureRepository, eventRepository, new StructureMapper());
        structure = new Structure();
        structure.setId(1L);
        structure.setNom("Institut Français");
        when(structureRepository.findById(1L)).thenReturn(Optional.of(structure));
        when(structureRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(structure));
        when(structureRepository.save(any(Structure.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void onlyAPlatformAdminCanCreateVerifyOrDeleteAStructure() {
        StructureRequestDTO dto = new StructureRequestDTO();
        dto.setNom("Nouvelle structure");

        assertThatThrownBy(() -> service.createStructure(dto, USER)).isInstanceOf(UnauthorizedAccesException.class);
        assertThatThrownBy(() -> service.verifyStructure(1L, ADMIN_OF_STRUCTURE_1)).isInstanceOf(UnauthorizedAccesException.class);
        assertThatThrownBy(() -> service.deleteStructure(1L, ADMIN_OF_STRUCTURE_1)).isInstanceOf(UnauthorizedAccesException.class);

        assertThat(service.verifyStructure(1L, ADMIN).getIsVerified()).isTrue();
    }

    @Test
    void aStructureAdminCanOnlyEditItsOwnStructure() {
        StructureRequestDTO dto = new StructureRequestDTO();
        dto.setNom("Nom modifié");

        assertThat(service.updateStructure(1L, dto, ADMIN_OF_STRUCTURE_1).getNom()).isEqualTo("Nom modifié");
        assertThatThrownBy(() -> service.updateStructure(1L, dto, ADMIN_OF_STRUCTURE_2))
                .isInstanceOf(UnauthorizedAccesException.class);
        assertThatThrownBy(() -> service.updateStructure(1L, dto, USER))
                .isInstanceOf(UnauthorizedAccesException.class);
    }

    @Test
    void structureWithEventsCannotBeDeleted() {
        when(structureRepository.existsById(1L)).thenReturn(true);
        when(eventRepository.existsByStructureId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.deleteStructure(1L, ADMIN)).isInstanceOf(InvalidEventDataException.class);
        verify(structureRepository, never()).deleteById(1L);
    }

    @Test
    void invitationCodeIsStoredHashedAndWorksOnlyOnce() {
        InvitationCodeResponseDto generated = service.generateInvitationCode(1L, ADMIN);
        String code = generated.getInvitationCode();

        assertThat(code).matches("[A-Z2-9]{4}-[A-Z2-9]{4}-[A-Z2-9]{4}");
        assertThat(structure.getInvitationCodeHash()).hasSize(64).doesNotContain(code.replace("-", ""));

        // Saisie tolérante : minuscules et espaces acceptés
        assertThat(service.checkInvitationCode(1L, code.toLowerCase().replace("-", " ")).isValid()).isTrue();
        assertThat(service.consumeInvitationCode(1L, code).isValid()).isTrue();
        // Deuxième utilisation refusée
        assertThat(service.consumeInvitationCode(1L, code).isValid()).isFalse();
    }

    @Test
    void wrongExpiredOrUnknownCodesAreRefused() {
        String code = service.generateInvitationCode(1L, ADMIN).getInvitationCode();

        assertThat(service.checkInvitationCode(1L, "AAAA-BBBB-CCCC").isValid()).isFalse();
        assertThat(service.checkInvitationCode(99L, code).isValid()).isFalse();

        structure.setInvitationCodeExpiresAt(LocalDateTime.now().minusMinutes(1));
        assertThat(service.consumeInvitationCode(1L, code).isValid()).isFalse();
    }

    @Test
    void onlyAPlatformAdminCanGenerateAnInvitationCode() {
        assertThatThrownBy(() -> service.generateInvitationCode(1L, ADMIN_OF_STRUCTURE_1))
                .isInstanceOf(UnauthorizedAccesException.class);
    }

    @Test
    void rolesAreComparedExactly() {
        UserContext fake = UserContext.of("x", "ROLE_ADMINISTRATEUR, ROLE_USER", null);
        assertThat(fake.isAdmin()).isFalse();
        assertThat(UserContext.of("x", "ROLE_USER, ROLE_ADMIN", null).isAdmin()).isTrue();
        assertThat(UserContext.of("x", "ROLE_STRUCTURE_ADMIN", "abc").canManageStructure(1L)).isFalse();
    }
}
