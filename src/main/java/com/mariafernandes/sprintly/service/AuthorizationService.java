package com.mariafernandes.sprintly.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.mariafernandes.sprintly.domain.Membership;
import com.mariafernandes.sprintly.domain.User;
import com.mariafernandes.sprintly.repository.MembershipRepository;

@Service
public class AuthorizationService {
    private final MembershipRepository membershipRepository;

    public AuthorizationService(MembershipRepository membershipRepository) {
        this.membershipRepository = membershipRepository;
    }

    public void requireAdmin(User user, Long organizationId) {
        Membership membership = membershipRepository.findByUserIdAndOrganizationId(user.getId(), organizationId)
            .orElseThrow(() -> new AccessDeniedException("Usuário não pertence a essa organização"));

        if (membership.getRole() != Membership.Role.ADMIN) {
            throw new AccessDeniedException("Ação restrita a administradores");
        }
    }

    public void requireMembership(User user, Long organizationId) {
        membershipRepository.findByUserIdAndOrganizationId(user.getId(), organizationId)
            .orElseThrow(() -> new AccessDeniedException("Usuário não pertence a essa organização"));
    }
}
