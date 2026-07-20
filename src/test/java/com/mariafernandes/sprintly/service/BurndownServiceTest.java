package com.mariafernandes.sprintly.service;

import com.mariafernandes.sprintly.domain.*;
import com.mariafernandes.sprintly.dto.BurndownResponse;
import com.mariafernandes.sprintly.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BurndownServiceTest {

    @Mock private SprintRepository sprintRepository;
    @Mock private TaskRepository taskRepository;
    @Mock private AuditLogRepository auditLogRepository;
    @Mock private StatusRepository statusRepository;

    @InjectMocks
    private BurndownService burndownService;

    @Test
    void calculate_deveRetornarTotalStoryPointsCorreto() {
        Sprint sprint = new Sprint("Sprint 1", null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 10));
        when(sprintRepository.findById(1L)).thenReturn(Optional.of(sprint));

        Task task1 = criarTask(1L, 5);
        Task task2 = criarTask(2L, 3);
        when(taskRepository.findBySprintId(1L)).thenReturn(List.of(task1, task2));

        when(auditLogRepository.findByEntityTypeAndEntityId("Task", 1L)).thenReturn(List.of());
        when(auditLogRepository.findByEntityTypeAndEntityId("Task", 2L)).thenReturn(List.of());

        BurndownResponse response = burndownService.calculate(1L);

        assertEquals(8, response.totalStoryPoints());
    }

    @Test
    void calculate_deveMostrarRemainingZeroQuandoTarefaFoiConcluidaAntesDoInicioDaSprint() {
        Sprint sprint = new Sprint("Sprint 1", null, LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 10));
        when(sprintRepository.findById(1L)).thenReturn(Optional.of(sprint));

        Task task = criarTask(1L, 5);
        when(taskRepository.findBySprintId(1L)).thenReturn(List.of(task));

        Status doneStatus = new Status("Done", 2, task.getBoard());
        doneStatus.setId(99L);
        doneStatus.setDone(true);

        AuditLog log = new AuditLog(null, "Task", 1L, "STATUS_CHANGE",
            "status: To Do(id=1) -> Done(id=99) | storyPoints: 5 | sprintId: 1");
        log.setCreatedAt(LocalDateTime.of(2026, 1, 3, 10, 0)); // antes do início da sprint

        when(auditLogRepository.findByEntityTypeAndEntityId("Task", 1L)).thenReturn(List.of(log));
        when(statusRepository.findByBoardIdOrderBySortOrder(task.getBoard().getId()))
            .thenReturn(List.of(doneStatus));

        BurndownResponse response = burndownService.calculate(1L);

        assertEquals(0, response.points().get(0).remaining());
    }

    @Test
    void calculate_deveMostrarRemainingCompletoQuandoTarefaNuncaFoiConcluida() {
        Sprint sprint = new Sprint("Sprint 1", null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5));
        when(sprintRepository.findById(1L)).thenReturn(Optional.of(sprint));

        Task task = criarTask(1L, 5);
        when(taskRepository.findBySprintId(1L)).thenReturn(List.of(task));
        when(auditLogRepository.findByEntityTypeAndEntityId("Task", 1L)).thenReturn(List.of());

        BurndownResponse response = burndownService.calculate(1L);

        response.points().forEach(point -> assertEquals(5, point.remaining()));
    }

    @Test
    void calculate_lancaExcecaoQuandoSprintNaoExiste() {
        when(sprintRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> burndownService.calculate(99L));
    }

    private Task criarTask(Long id, Integer storyPoints) {
        Organization org = new Organization("Org");
        org.setId(1L);
        Team team = new Team("Time", org);
        Project project = new Project("Projeto", team);
        Board board = new Board("Board", project);
        board.setId(1L);
        Status status = new Status("To Do", 1, board);

        Task task = new Task("Tarefa " + id, TaskType.TASK, Priority.MEDIUM, board, status, null);
        task.setId(id);
        task.setStoryPoints(storyPoints);
        return task;
    }
}