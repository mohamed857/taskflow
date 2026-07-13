package com.taskflow;

import com.taskflow.entity.User;
import com.taskflow.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
class UserRepositoryTests {

    // هنا بنطلب من الـ Testcontainers يعمل نسخة PostgreSQL حقيقية
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    // هنا بنخلي Spring Boot يستبدل الـ database الموجود في application.properties 
    // بالـ database اللي اتعمل في الـ Container فوق
    @org.springframework.test.context.DynamicPropertySource
    static void postgresProperties(org.springframework.test.context.DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveUser() {
        // 1. Prepare
        User user = User.builder()
                .username("mohamed")
                .email("mohamed@test.com")
                .build();

        // 2. Action
        User savedUser = userRepository.save(user);

        // 3. Assert
        assertNotNull(savedUser.getId());
        assertEquals("mohamed", savedUser.getUsername());
        assertTrue(userRepository.existsByEmail("mohamed@test.com"));
    }
}