package com.predictorama.backend.adapter.rest.controller;

import com.predictorama.backend.adapter.rest.SessionService;
import com.predictorama.backend.adapter.rest.dto.*;
import com.predictorama.backend.adapter.rest.mapper.GroupMemberMapper;
import com.predictorama.backend.adapter.rest.mapper.GroupMapper;
import com.predictorama.backend.domain.service.GroupService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private static final Logger log = LoggerFactory.getLogger(GroupController.class);

    private final GroupService groupService;
    private final SessionService sessionService;

    @PostMapping
    public ResponseEntity<GroupResponseDto> createGroup(@RequestBody CreateGroupRequestDto request, HttpSession session) {
        return sessionService.getUserId(session)
                .map(userId -> {
                    log.info("POST /api/groups - userId={}, name={}", userId, request.getName());
                    GroupResponseDto response = GroupMapper.toResponse(groupService.createGroup(userId, request.getName(), request.getDescription()));
                    log.info("Group created - id={}", response.getId());
                    return ResponseEntity.status(HttpStatus.CREATED).body(response);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/join")
    public ResponseEntity<GroupMemberResponseDto> joinGroup(@RequestBody JoinGroupRequestDto request, HttpSession session) {
        return sessionService.getUserId(session)
                .map(userId -> {
                    log.info("POST /api/groups/join - userId={}, inviteCode={}", userId, request.getInviteCode());
                    return groupService.joinGroup(userId, request.getInviteCode())
                            .map(member -> {
                                log.info("User joined group - memberId={}", member.getId());
                                return ResponseEntity.ok(GroupMemberMapper.toResponse(member));
                            })
                            .orElseGet(() -> {
                                log.warn("Join failed - invite code not found: {}", request.getInviteCode());
                                return ResponseEntity.<GroupMemberResponseDto>notFound().build();
                            });
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<Void> leaveGroup(@PathVariable UUID groupId, HttpSession session) {
        var userId = sessionService.getUserId(session);
        if (userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("DELETE /api/groups/{}/leave - userId={}", groupId, userId.get());
        groupService.leaveGroup(userId.get(), groupId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    public ResponseEntity<List<UserGroupsResponseDto>> getMyGroups(HttpSession session) {
        return sessionService.getUserId(session)
                .map(userId -> {
                    log.info("GET /api/groups/my - userId={}", userId);
                    List<UserGroupsResponseDto> response = groupService.getUserGroups(userId).stream()
                            .map(GroupMapper::toUserGroupsResponse)
                            .toList();
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}
