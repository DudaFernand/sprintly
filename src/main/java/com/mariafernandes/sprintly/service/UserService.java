package com.mariafernandes.sprintly.service;

import com.mariafernandes.sprintly.domain.Membership;
import com.mariafernandes.sprintly.domain.User;
import com.mariafernandes.sprintly.dto.UserResponse;
import com.mariafernandes.sprintly.repository.MembershipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class UserService {

    private final MembershipRepository membershipRepository;
    private final AuthorizationService authorizationService;

    public UserService(MembershipRepository membershipRepository,
                       AuthorizationService authorizationService) {
        this.membershipRepository = membershipRepository;
        this.authorizationService = authorizationService;
    }

    /**
     * Lista usuários da organização. Só quem tem membership nela enxerga a lista.
     */
    @Transactional(readOnly = true)
    public List<UserResponse> listByOrganization(User currentUser, Long organizationId) {
        authorizationService.requireMembership(currentUser, organizationId);

        return membershipRepository.findByOrganizationId(organizationId).stream()
            .map(Membership::getUser)
            .map(UserResponse::from)
            .sorted(Comparator.comparing(UserResponse::email))
            .toList();
    }
}
