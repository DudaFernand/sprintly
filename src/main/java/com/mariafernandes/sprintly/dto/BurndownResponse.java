package com.mariafernandes.sprintly.dto;

import java.time.LocalDate;
import java.util.List;

public record BurndownResponse(LocalDate sprintStart, LocalDate sprintEnd, int totalStoryPoints, List<BurndownPoint> points) {}