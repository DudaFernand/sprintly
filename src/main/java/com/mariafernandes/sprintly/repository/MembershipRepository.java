package com.mariafernandes.sprintly.repository;

import com.mariafernandes.sprintly.domain.Membership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    List<Membership> findByOrganizationId(Long organizationId);
    List<Membership> findByUserId(Long userId);
}