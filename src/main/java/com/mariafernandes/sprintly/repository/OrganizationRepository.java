package com.mariafernandes.sprintly.repository;

import com.mariafernandes.sprintly.domain.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

}