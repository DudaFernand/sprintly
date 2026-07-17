package com.mariafernandes.sprintly.repository;

import com.mariafernandes.sprintly.domain.Epic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EpicRepository extends JpaRepository<Epic, Long> {
}