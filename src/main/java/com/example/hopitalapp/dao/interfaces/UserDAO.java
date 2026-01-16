package com.example.hopitalapp.dao.interfaces;

import com.example.hopitalapp.model.User;
import java.util.List;

public interface UserDAO {
    User findByEmail(String email);
    User findById(int id);
    List<User> findAll();
    void save(User user);
    void update(User user);
    void delete(int id);
    User authenticate(String email, String password);
}
