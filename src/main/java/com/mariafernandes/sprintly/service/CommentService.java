package com.mariafernandes.sprintly.service;

import com.mariafernandes.sprintly.domain.*;
import com.mariafernandes.sprintly.dto.CreateCommentRequest;
import com.mariafernandes.sprintly.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CommentService {

    private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+(?:\\.\\w+)?@\\w+\\.\\w+)");

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CommentMentionRepository mentionRepository;
    private final AuthorizationService authorizationService;
    private final NotificationPublisher notificationPublisher;

    public CommentService(CommentRepository commentRepository, TaskRepository taskRepository,
                           UserRepository userRepository, CommentMentionRepository mentionRepository,
                           AuthorizationService authorizationService, NotificationPublisher notificationPublisher) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.mentionRepository = mentionRepository;
        this.authorizationService = authorizationService;
        this.notificationPublisher = notificationPublisher;
    }

    public Comment create(CreateCommentRequest request, User author) {
        Task task = taskRepository.findById(request.taskId())
            .orElseThrow(() -> new IllegalArgumentException("Tarefa não encontrada"));

        Long organizationId = task.getBoard().getProject().getTeam().getOrganization().getId();
        authorizationService.requireMembership(author, organizationId);

        Comment comment = new Comment(request.content(), task, author);
        commentRepository.save(comment);

        extractMentions(request.content()).forEach(email ->
            userRepository.findByEmail(email).ifPresent(mentionedUser -> {
                CommentMention mention = new CommentMention(comment, mentionedUser);
                mentionRepository.save(mention);

                notificationPublisher.publish(
                    mentionedUser.getId(),
                    "mention",
                    author.getUsername() + " mencionou você em um comentário: \"" + request.content() + "\""
                );
            })
        );

        return comment;
    }

    public List<Comment> findByTask(Long taskId, User currentUser) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Tarefa não encontrada"));
        Long organizationId = task.getBoard().getProject().getTeam().getOrganization().getId();
        authorizationService.requireMembership(currentUser, organizationId);
        return commentRepository.findByTaskIdOrderByCreatedAt(taskId);
    }

    private List<String> extractMentions(String content) {
        Matcher matcher = MENTION_PATTERN.matcher(content);
        return matcher.results()
            .map(match -> match.group(1))
            .toList();
    }
}
