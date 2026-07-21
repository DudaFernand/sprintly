package com.mariafernandes.sprintly.repository;

import com.mariafernandes.sprintly.domain.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    List<Membership> findByOrganizationId(Long organizationId);

    @Query("SELECT m FROM Membership m JOIN FETCH m.organization WHERE m.user.id = :userId")
    List<Membership> findByUserId(@Param("userId") Long userId);

    Optional<Membership> findByUserIdAndOrganizationId(Long userId, Long organizationId);
}