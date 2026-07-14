package com.taskflow.service;

import com.taskflow.dto.UserRequest;
import com.taskflow.dto.UserResponse;
import com.taskflow.entity.User;
import com.taskflow.exception.UserAlreadyExistsException;
import com.taskflow.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public UserResponse createUser(UserRequest req){
        if(userRepository.existsByEmail(req.email())){
            throw new UserAlreadyExistsException("User with email " + req.email() + " already exists");
        }
        User user = User.builder()
                .username(req.username())
                .email(req.email())
                .build();
        User savedUser= userRepository.save(user);
        return new UserResponse(savedUser.getId(), savedUser.getUsername(), savedUser.getEmail());
    }
}
