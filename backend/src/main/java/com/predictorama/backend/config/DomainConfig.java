package com.predictorama.backend.config;

import com.predictorama.backend.domain.port.persistence.GroupMemberRepositoryPort;
import com.predictorama.backend.domain.port.persistence.GroupRepositoryPort;
import com.predictorama.backend.domain.port.persistence.UserRepositoryPort;
import com.predictorama.backend.domain.service.GroupService;
import com.predictorama.backend.domain.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {

    @Bean
    public UserService userService(UserRepositoryPort userRepository) {
        return new UserService(userRepository);
    }

    @Bean
    public GroupService groupService(GroupRepositoryPort groupRepository, GroupMemberRepositoryPort groupMemberRepository) {
        return new GroupService(groupRepository, groupMemberRepository);
    }
}
