package com.mariafernandes.sprintly.service;

import com.mariafernandes.sprintly.domain.*;
import com.mariafernandes.sprintly.dto.CreateCommentRequest;
import com.mariafernandes.sprintly.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock private CommentRepository commentRepository;
    @Mock private TaskRepository taskRepository;
    @Mock private UserRepository userRepository;
    @Mock private CommentMentionRepository mentionRepository;
    @Mock private AuthorizationService authorizationService;
    @Mock private NotificationPublisher notificationPublisher;

    @InjectMocks
    private CommentService commentService;

    @Test
    void create_devePublicarNotificacaoQuandoTemMencaoValida() {
        Task task = new Task();
        Board board = new Board();
        Project project = new Project();
        Team team = new Team();
        Organization org = new Organization();
        org.setId(1L);
        team.setOrganization(org);
        project.setTeam(team);
        board.setProject(project);
        task.setBoard(board);

        User author = new User("autor@teste.com", "senha");
        User mentioned = new User("mencionado@teste.com", "senha");
        mentioned.setId(2L);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail("mencionado@teste.com")).thenReturn(Optional.of(mentioned));

        CreateCommentRequest request = new CreateCommentRequest("Oi @mencionado@teste.com, olha isso", 1L);

        commentService.create(request, author);

        verify(notificationPublisher, times(1)).publish(eq(2L), eq("mention"), any());
        verify(mentionRepository, times(1)).save(any());
    }

    @Test
    void create_naoDevePublicarNotificacaoQuandoNaoTemMencao() {
        Task task = new Task();
        Board board = new Board();
        Project project = new Project();
        Team team = new Team();
        Organization org = new Organization();
        org.setId(1L);
        team.setOrganization(org);
        project.setTeam(team);
        board.setProject(project);
        task.setBoard(board);

        User author = new User("autor@teste.com", "senha");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        CreateCommentRequest request = new CreateCommentRequest("Comentário sem menção nenhuma", 1L);

        commentService.create(request, author);

        verify(notificationPublisher, never()).publish(any(), any(), any());
    }

    @Test
    void create_naoDevePublicarNotificacaoQuandoUsuarioMencionadoNaoExiste() {
        Task task = new Task();
        Board board = new Board();
        Project project = new Project();
        Team team = new Team();
        Organization org = new Organization();
        org.setId(1L);
        team.setOrganization(org);
        project.setTeam(team);
        board.setProject(project);
        task.setBoard(board);

        User author = new User("autor@teste.com", "senha");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail("naoexiste@teste.com")).thenReturn(Optional.empty());

        CreateCommentRequest request = new CreateCommentRequest("Oi @naoexiste@teste.com", 1L);

        commentService.create(request, author);

        verify(notificationPublisher, never()).publish(any(), any(), any());
    }
}