package com.example.userapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository repo;

    public List<User> getAll() { return repo.findAll(); }
    public User create(User u) { return repo.save(u); }
    public void delete(Long id) { repo.deleteById(id); }
}

