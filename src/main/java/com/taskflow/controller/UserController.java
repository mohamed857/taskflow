package com.taskflow.controller;

import com.taskflow.dto.UserRequest;
import com.taskflow.dto.UserResponse;
import com.taskflow.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/users")
@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
//
//    @GetMapping()
//    public ResponseEntity<UserResponse> getUsers(){
//        return userService.
//    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request){
        UserResponse response =  userService.createUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
