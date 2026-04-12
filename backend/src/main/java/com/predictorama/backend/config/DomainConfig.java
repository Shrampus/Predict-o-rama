package com.predictorama.backend.config;

import com.predictorama.backend.domain.port.PasswordVerifier;
import com.predictorama.backend.domain.port.persistence.GroupMemberRepositoryPort;
import com.predictorama.backend.domain.port.persistence.GroupRepositoryPort;
import com.predictorama.backend.domain.port.persistence.UserRepositoryPort;
import com.predictorama.backend.domain.service.AuthService;
import com.predictorama.backend.domain.service.GroupService;
import com.predictorama.backend.domain.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class DomainConfig {

    // TODO: remove default password once real auth (Google OAuth) is in place
    private static final String DEFAULT_PASSWORD = "predictorama123";

    @Bean
    public UserService userService(UserRepositoryPort userRepository) {
        String defaultPasswordHash = new BCryptPasswordEncoder().encode(DEFAULT_PASSWORD);
        return new UserService(userRepository, defaultPasswordHash);
    }

    @Bean
    public GroupService groupService(GroupRepositoryPort groupRepository, GroupMemberRepositoryPort groupMemberRepository) {
        return new GroupService(groupRepository, groupMemberRepository);
    }

    @Bean
    public AuthService authService(UserRepositoryPort userRepository, PasswordVerifier passwordVerifier) {
        return new AuthService(userRepository, passwordVerifier);
    }
}
