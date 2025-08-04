package com.user.user.service;

import com.user.user.model.User;

import java.util.Optional;

public interface DBService {
    Optional<User> getUser(String username);
}
