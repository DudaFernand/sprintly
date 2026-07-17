package com.mariafernandes.sprintly.controller;

import com.mariafernandes.sprintly.domain.Membership;
import com.mariafernandes.sprintly.domain.User;
import com.mariafernandes.sprintly.repository.MembershipRepository;
import com.mariafernandes.sprintly.service.AuthorizationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/memberships")
public class MembershipController {

    private final MembershipRepository repository;
    private final AuthorizationService authorizationService;

    public MembershipController(MembershipRepository repository, AuthorizationService authorizationService) {
        this.repository = repository;
        this.authorizationService = authorizationService;
    }

    @PostMapping
    public Membership create(@RequestBody Membership membership, @AuthenticationPrincipal User currentUser) {
        authorizationService.requireAdmin(currentUser, membership.getOrganization().getId());
        return repository.save(membership);
    }

    @GetMapping
    public List<Membership> findByOrganization(@RequestParam Long organizationId, @AuthenticationPrincipal User currentUser) {
        authorizationService.requireMembership(currentUser, organizationId);
        return repository.findByOrganizationId(organizationId);
    }
}