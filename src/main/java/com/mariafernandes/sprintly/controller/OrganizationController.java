package com.mariafernandes.sprintly.controller;

import com.mariafernandes.sprintly.domain.Membership;
import com.mariafernandes.sprintly.domain.Organization;
import com.mariafernandes.sprintly.domain.User;
import com.mariafernandes.sprintly.repository.MembershipRepository;
import com.mariafernandes.sprintly.repository.OrganizationRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organizations")
public class OrganizationController {

    private final OrganizationRepository organizationRepository;
    private final MembershipRepository membershipRepository;

    public OrganizationController(OrganizationRepository organizationRepository,
                                   MembershipRepository membershipRepository) {
        this.organizationRepository = organizationRepository;
        this.membershipRepository = membershipRepository;
    }

    @PostMapping
    public Organization create(@RequestBody Organization organization, @AuthenticationPrincipal User currentUser) {
        Organization saved = organizationRepository.save(organization);
        Membership membership = new Membership(currentUser, saved, Membership.Role.ADMIN);
        membershipRepository.save(membership);
        return saved;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<Organization> findMine(@AuthenticationPrincipal User currentUser) {
        return membershipRepository.findByUserId(currentUser.getId()).stream()
            .map(Membership::getOrganization)
            .toList();
    }
}
