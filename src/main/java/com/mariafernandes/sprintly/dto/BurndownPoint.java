package com.mariafernandes.sprintly.dto;

import java.time.LocalDate;

public record BurndownPoint(LocalDate date, int remaining, double ideal) {}