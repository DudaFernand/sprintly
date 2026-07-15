package com.mariafernandes.sprintly.controller;

import com.mariafernandes.sprintly.domain.Membership;
import com.mariafernandes.sprintly.repository.MembershipRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/memberships")
public class MembershipController {

    private final MembershipRepository repository;

    public MembershipController(MembershipRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public Membership create(@RequestBody Membership membership) {
        return repository.save(membership);
    }

    @GetMapping
    public List<Membership> findByOrganization(@RequestParam Long organizationId) {
        return repository.findByOrganizationId(organizationId);
    }
}