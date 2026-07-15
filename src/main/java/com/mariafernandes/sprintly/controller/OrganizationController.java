package com.mariafernandes.sprintly.controller;

import com.mariafernandes.sprintly.domain.Organization;
import com.mariafernandes.sprintly.repository.OrganizationRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organizations")
public class OrganizationController {

    private final OrganizationRepository repository;

    public OrganizationController(OrganizationRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public Organization create(@RequestBody Organization organization) {
        return repository.save(organization);
    }

    @GetMapping
    public List<Organization> findAll() {
        return repository.findAll();
    }
}