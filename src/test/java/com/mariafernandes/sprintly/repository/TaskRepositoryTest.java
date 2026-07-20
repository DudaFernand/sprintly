package com.mariafernandes.sprintly.repository;

import com.mariafernandes.sprintly.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TaskRepositoryTest {

    @Autowired private TaskRepository taskRepository;
    @Autowired private BoardRepository boardRepository;
    @Autowired private StatusRepository statusRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private TeamRepository teamRepository;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private UserRepository userRepository;

    @Test
    void countByStatusId_deveContarTarefasCorretamente() {
        Organization org = organizationRepository.save(new Organization("Org Teste"));
        Team team = teamRepository.save(new Team("Time Teste", org));
        Project project = projectRepository.save(new Project("Projeto Teste", team));
        Board board = boardRepository.save(new Board("Board Teste", project));
        Status status = statusRepository.save(new Status("To Do", 1, board));
        User reporter = userRepository.save(new User("reporter@teste.com", "senha"));

        taskRepository.save(new Task("Tarefa 1", TaskType.TASK, Priority.LOW, board, status, reporter));
        taskRepository.save(new Task("Tarefa 2", TaskType.BUG, Priority.HIGH, board, status, reporter));

        long count = taskRepository.countByStatusId(status.getId());

        assertEquals(2, count);
    }

    @Test
    void countByStatusId_deveRetornarZeroQuandoNaoHaTarefas() {
        Organization org = organizationRepository.save(new Organization("Org Vazia"));
        Team team = teamRepository.save(new Team("Time Vazio", org));
        Project project = projectRepository.save(new Project("Projeto Vazio", team));
        Board board = boardRepository.save(new Board("Board Vazio", project));
        Status status = statusRepository.save(new Status("Done", 2, board));

        long count = taskRepository.countByStatusId(status.getId());

        assertEquals(0, count);
    }

    @Test
    void findByBoardId_deveRetornarApenasTarefasDoBoard() {
        Organization org = organizationRepository.save(new Organization("Org"));
        Team team = teamRepository.save(new Team("Time", org));
        Project project = projectRepository.save(new Project("Projeto", team));
        Board board1 = boardRepository.save(new Board("Board 1", project));
        Board board2 = boardRepository.save(new Board("Board 2", project));
        Status status1 = statusRepository.save(new Status("To Do", 1, board1));
        Status status2 = statusRepository.save(new Status("To Do", 1, board2));
        User reporter = userRepository.save(new User("reporter2@teste.com", "senha"));

        taskRepository.save(new Task("Tarefa da board 1", TaskType.TASK, Priority.LOW, board1, status1, reporter));
        taskRepository.save(new Task("Tarefa da board 2", TaskType.TASK, Priority.LOW, board2, status2, reporter));

        var tasksBoard1 = taskRepository.findByBoardId(board1.getId());

        assertEquals(1, tasksBoard1.size());
        assertEquals("Tarefa da board 1", tasksBoard1.get(0).getTitle());
    }
}