package com.taskflow.controller;

import com.taskflow.dto.UserRequest;
import com.taskflow.dto.UserResponse;
import com.taskflow.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@WebMvcTest(UserController.class)
public class UserControllerTests {
    @Autowired
    private MockMvc mockMvc;    // أداة عشان نبعت Request وهمي للـ API

    @Autowired
    private ObjectMapper objectMapper;  // لتحويل الـ Java Object لـ JSON

    @MockitoBean
    private UserService userService; // نعمل Mock للـ Service عشان نعزل الاختبار

    @Test
    void shouldCreateUserAndReturn201() throws Exception{
        // 1. Prepare
        UserRequest request = new UserRequest("ahmed","ahmed@test.com");

        Mockito.when(userService.createUser(any(UserRequest.class)))
                .thenReturn(new UserResponse(1L,"ahmed","ahmed@test.com"));

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())  // نتوقع رقم 201
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("ahmed"));
    }

    @Test
    void shouldReturn400WhenEmailIsInvalid() throws Exception {
        // 1. Prepare (إيميل غلط عمداً)
        UserRequest request = new UserRequest("ahmed", "invalid-email");

        // 2. Action & Assert
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()) // نتوقع رقم 400
                .andExpect(jsonPath("$.email").exists()); // نتوقع رسالة خطأ في حقل الإيميل
    }
}
