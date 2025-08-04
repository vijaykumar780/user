package com.user.user.service;

import com.user.user.model.User;
import com.user.user.repo.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DBServiceImpl implements DBService {

    /*private UserRepository userRepository;
    public DBServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }*/

    @Override
    public Optional<User> getUser(String username) {
        //return userRepository.findByUsername(username);

        return Optional.ofNullable(User.builder().username("Vijay").id(1L).password("pass").build());
    }
}
