package com.mariafernandes.sprintly.service;

import com.mariafernandes.sprintly.domain.Membership;
import com.mariafernandes.sprintly.domain.User;
import com.mariafernandes.sprintly.repository.MembershipRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private MembershipRepository membershipRepository;

    @InjectMocks
    private AuthorizationService authorizationService;

    @Test
    void requireAdmin_devePermitirQuandoUsuarioEhAdmin() {
        User user = new User("admin@teste.com", "senha");
        user.setId(1L);
        Membership membership = new Membership(user, null, Membership.Role.ADMIN);

        when(membershipRepository.findByUserIdAndOrganizationId(1L, 1L))
            .thenReturn(Optional.of(membership));

        assertDoesNotThrow(() -> authorizationService.requireAdmin(user, 1L));
    }

    @Test
    void requireAdmin_deveLancarExcecaoQuandoUsuarioEhMembroComum() {
        User user = new User("membro@teste.com", "senha");
        user.setId(2L);
        Membership membership = new Membership(user, null, Membership.Role.MEMBER);

        when(membershipRepository.findByUserIdAndOrganizationId(2L, 1L))
            .thenReturn(Optional.of(membership));

        assertThrows(AccessDeniedException.class, () -> authorizationService.requireAdmin(user, 1L));
    }

    @Test
    void requireAdmin_deveLancarExcecaoQuandoUsuarioNaoTemMembership() {
        User user = new User("semvinculo@teste.com", "senha");
        user.setId(3L);

        when(membershipRepository.findByUserIdAndOrganizationId(3L, 1L))
            .thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> authorizationService.requireAdmin(user, 1L));
    }

    @Test
    void requireMembership_devePermitirQuandoUsuarioEhMembroComum() {
        User user = new User("membro@teste.com", "senha");
        user.setId(4L);
        Membership membership = new Membership(user, null, Membership.Role.MEMBER);

        when(membershipRepository.findByUserIdAndOrganizationId(4L, 1L))
            .thenReturn(Optional.of(membership));

        assertDoesNotThrow(() -> authorizationService.requireMembership(user, 1L));
    }
}
