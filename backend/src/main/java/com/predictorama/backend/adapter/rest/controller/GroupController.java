package com.predictorama.backend.adapter.rest.controller;

import com.predictorama.backend.adapter.rest.dto.CreateGroupRequest;
import com.predictorama.backend.adapter.rest.dto.GroupMemberResponse;
import com.predictorama.backend.adapter.rest.dto.GroupResponse;
import com.predictorama.backend.adapter.rest.dto.JoinGroupRequest;
import com.predictorama.backend.adapter.rest.mapper.GroupMemberRestMapper;
import com.predictorama.backend.adapter.rest.mapper.GroupRestMapper;
import com.predictorama.backend.domain.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private static final Logger log = LoggerFactory.getLogger(GroupController.class);

    private final GroupService groupService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupResponse createGroup(@RequestBody CreateGroupRequest request) {
        log.info("POST /api/groups - ownerId={}, name={}", request.getOwnerId(), request.getName());
        GroupResponse response = GroupRestMapper.toResponse(groupService.createGroup(request.getOwnerId(), request.getName(), request.getDescription()));
        log.info("Group created - id={}", response.getId());
        return response;
    }

    @PostMapping("/join")
    public ResponseEntity<GroupMemberResponse> joinGroup(@RequestBody JoinGroupRequest request) {
        log.info("POST /api/groups/join - userId={}, inviteCode={}", request.getUserId(), request.getInviteCode());
        return groupService.joinGroup(request.getUserId(), request.getInviteCode())
                .map(member -> {
                    log.info("User joined group - memberId={}", member.getId());
                    return ResponseEntity.ok(GroupMemberRestMapper.toResponse(member));
                })
                .orElseGet(() -> {
                    log.warn("Join failed - invite code not found: {}", request.getInviteCode());
                    return ResponseEntity.notFound().build();
                });
    }
}
