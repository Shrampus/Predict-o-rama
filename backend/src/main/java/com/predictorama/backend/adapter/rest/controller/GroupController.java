package com.predictorama.backend.adapter.rest.controller;

import com.predictorama.backend.adapter.rest.SessionService;
import com.predictorama.backend.adapter.rest.dto.*;
import com.predictorama.backend.adapter.rest.mapper.GroupMemberMapper;
import com.predictorama.backend.adapter.rest.mapper.GroupMapper;
import com.predictorama.backend.domain.service.GroupService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private static final Logger log = LoggerFactory.getLogger(GroupController.class);

    private final GroupService groupService;
    private final SessionService sessionService;

    private <T> ResponseEntity<T> withUser(HttpSession session, Function<UUID, ResponseEntity<T>> action) {
        return sessionService.getUserId(session)
                .map(action)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping
    public ResponseEntity<GroupResponseDto> createGroup(@RequestBody CreateGroupRequestDto request, HttpSession session) {
        return withUser(session, userId -> {
            log.info("POST /api/groups - userId={}, name={}", userId, request.getName());
            GroupResponseDto response = GroupMapper.toResponse(groupService.createGroup(userId, request.getName(), request.getDescription()));
            log.info("Group created - id={}", response.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        });
    }

    @PostMapping("/join")
    public ResponseEntity<GroupMemberResponseDto> joinGroup(@RequestBody JoinGroupRequestDto request, HttpSession session) {
        return withUser(session, userId -> {
            log.info("POST /api/groups/join - userId={}, inviteCode={}", userId, request.getInviteCode());
            return groupService.joinGroup(userId, request.getInviteCode())
                    .map(member -> {
                        log.info("User joined group - memberId={}", member.getId());
                        return ResponseEntity.ok(GroupMemberMapper.toResponse(member));
                    })
                    .orElseGet(() -> {
                        log.warn("Join failed - invite code not found: {}", request.getInviteCode());
                        return ResponseEntity.notFound().build();
                    });
        });
    }

    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<Void> leaveGroup(@PathVariable UUID groupId, HttpSession session) {
        return withUser(session, userId -> {
            log.info("DELETE /api/groups/{}/leave - userId={}", groupId, userId);
            groupService.leaveGroup(userId, groupId);
            return ResponseEntity.noContent().build();
        });
    }

    @GetMapping("/my")
    public ResponseEntity<List<UserGroupsResponseDto>> getMyGroups(HttpSession session) {
        return withUser(session, userId -> {
            log.info("GET /api/groups/my - userId={}", userId);
            List<UserGroupsResponseDto> response = groupService.getUserGroups(userId).stream()
                    .map(GroupMapper::toUserGroupsResponse)
                    .toList();
            return ResponseEntity.ok(response);
        });
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDetailsResponse> getGroupDetails(@PathVariable UUID groupId, HttpSession session) {
        return withUser(session, userId -> {
            var details = groupService.getGroupDetails(userId, groupId);
            return ResponseEntity.ok(new GroupDetailsResponse(
                    details.getGroup().getId(),
                    details.getGroup().getName(),
                    details.getGroup().getDescription(),
                    details.getCurrentUserRole()
            ));
        });
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMemberResponseDto>> getGroupMembers(@PathVariable UUID groupId, HttpSession session) {
        return withUser(session, userId -> ResponseEntity.ok(groupService.getGroupMembers(userId, groupId).stream()
                .map(GroupMemberMapper::toResponse)
                .toList()));
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<GroupMemberResponseDto> addGroupMember(
            @PathVariable UUID groupId,
            @Valid @RequestBody AddGroupMemberRequest request,
            HttpSession session
    ) {
        return withUser(session, userId -> {
            GroupMemberResponseDto response = GroupMemberMapper.toResponse(
                    groupService.addMemberByEmail(userId, groupId, request.getEmail().trim())
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        });
    }

    @GetMapping("/{groupId}/tournaments")
    public ResponseEntity<List<GroupTournamentResponse>> getGroupTournaments(@PathVariable UUID groupId, HttpSession session) {
        return withUser(session, userId -> ResponseEntity.ok(
                groupService.getGroupTournaments(userId, groupId).stream()
                        .map(tournament -> new GroupTournamentResponse(
                                tournament.getId(),
                                tournament.getName(),
                                tournament.getDescription(),
                                tournament.getSport()
                        ))
                        .toList()
        ));
    }

    @PostMapping("/{groupId}/tournaments")
    public ResponseEntity<Void> addGroupTournament(
            @PathVariable UUID groupId,
            @Valid @RequestBody AddGroupTournamentRequest request,
            HttpSession session
    ) {
        return withUser(session, userId -> {
            groupService.addTournamentToGroup(userId, groupId, request.getTournamentId());
            return ResponseEntity.status(HttpStatus.CREATED).<Void>build();
        });
    }
}
