package com.mariafernandes.sprintly.service;

import com.mariafernandes.sprintly.domain.*;
import com.mariafernandes.sprintly.dto.BurndownPoint;
import com.mariafernandes.sprintly.dto.BurndownResponse;
import com.mariafernandes.sprintly.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BurndownService {

    private static final Pattern CHANGE_PATTERN =
        Pattern.compile("status: .* -> (.+)\\(id=\\d+\\) \\| storyPoints: (\\d+|null)");

    private final SprintRepository sprintRepository;
    private final TaskRepository taskRepository;
    private final AuditLogRepository auditLogRepository;
    private final StatusRepository statusRepository;

    public BurndownService(SprintRepository sprintRepository, TaskRepository taskRepository,
                            AuditLogRepository auditLogRepository, StatusRepository statusRepository) {
        this.sprintRepository = sprintRepository;
        this.taskRepository = taskRepository;
        this.auditLogRepository = auditLogRepository;
        this.statusRepository = statusRepository;
    }

    public BurndownResponse calculate(Long sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
            .orElseThrow(() -> new IllegalArgumentException("Sprint não encontrada"));

        List<Task> tasks = taskRepository.findBySprintId(sprintId);
        int totalStoryPoints = tasks.stream()
            .mapToInt(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
            .sum();

        List<BurndownPoint> points = new java.util.ArrayList<>();
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(sprint.getStartDate(), sprint.getEndDate());

        for (LocalDate day = sprint.getStartDate(); !day.isAfter(sprint.getEndDate()); day = day.plusDays(1)) {
            int completedByDay = completedStoryPointsUntil(tasks, day);
            int remaining = totalStoryPoints - completedByDay;

            long daysElapsed = java.time.temporal.ChronoUnit.DAYS.between(sprint.getStartDate(), day);
            double ideal = totalDays == 0 ? 0 :
                totalStoryPoints - (totalStoryPoints * daysElapsed / (double) totalDays);

            points.add(new BurndownPoint(day, remaining, ideal));
        }

        return new BurndownResponse(sprint.getStartDate(), sprint.getEndDate(), totalStoryPoints, points);
    }

    private int completedStoryPointsUntil(List<Task> tasks, LocalDate day) {
        LocalDateTime cutoff = day.plusDays(1).atStartOfDay();
        int total = 0;

        for (Task task : tasks) {
            List<AuditLog> logs = auditLogRepository.findByEntityTypeAndEntityId("Task", task.getId());

            boolean wasDone = logs.stream()
                .filter(log -> "STATUS_CHANGE".equals(log.getAction()))
                .filter(log -> log.getCreatedAt().isBefore(cutoff))
                .anyMatch(log -> isTransitionToDoneStatus(log.getChanges(), task));

            if (wasDone) {
                total += task.getStoryPoints() != null ? task.getStoryPoints() : 0;
            }
        }
        return total;
    }

    private boolean isTransitionToDoneStatus(String changes, Task task) {
        Matcher matcher = CHANGE_PATTERN.matcher(changes);
        if (!matcher.find()) return false;

        String newStatusName = matcher.group(1);
        return statusRepository.findByBoardIdOrderBySortOrder(task.getBoard().getId()).stream()
            .anyMatch(s -> s.getName().equals(newStatusName) && Boolean.TRUE.equals(s.getDone()));
    }
}