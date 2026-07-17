package com.taskflow.config;

import com.taskflow.entity.User;
import com.taskflow.repository.UserRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Cacheable(value = "users", key = "#email")
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println(" ---> Going to DATABASE to fetch user: " + email); // عشان نتأكد إنه بيدخل الداتا بيز مرة واحدة بس
        User user = userRepository.findByEmail(email).orElseThrow(()-> new UsernameNotFoundException("There is no user with this email "+ email));
        return new UserPrincipal(user);
    }
}
