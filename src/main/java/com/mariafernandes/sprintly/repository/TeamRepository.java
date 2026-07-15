package com.mariafernandes.sprintly.repository;

import com.mariafernandes.sprintly.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByOrganizationId(Long organizationId);
}