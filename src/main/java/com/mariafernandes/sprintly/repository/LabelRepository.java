package com.mariafernandes.sprintly.repository;

import com.mariafernandes.sprintly.domain.Label;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabelRepository extends JpaRepository<Label, Long> {
}