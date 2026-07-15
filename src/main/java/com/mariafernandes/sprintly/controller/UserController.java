package com.mariafernandes.sprintly.controller;

import com.mariafernandes.sprintly.domain.User;
import com.mariafernandes.sprintly.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository repository;

    public UserController(UserRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public User create(@RequestBody User user) {
        return repository.save(user);
    }

    @GetMapping
    public List<User> findAll() {
        return repository.findAll();
    }
}