package com.taskflow.controller;

import com.taskflow.config.JwtService;
import com.taskflow.config.UserPrincipal;
import com.taskflow.dto.UserRequest;
import com.taskflow.dto.UserResponse;
import com.taskflow.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService, JwtService jweService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtService = jweService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody UserRequest request){
        UserResponse response = userService.createUser(request);
//        return  ResponseEntity.created().body(response);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @PostMapping("/login")
    public ResponseEntity<Map<String,String>> login(@RequestBody UserRequest request){
            var authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(),request.password()));

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            String role = principal.getRole();
            String token = jwtService.generateToken(request.email(), role);
            return ResponseEntity.ok(Map.of("token",token,  "role", role));
    }

}
