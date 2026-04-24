package com.predictorama.backend.adapter.rest.controller;

import com.predictorama.backend.adapter.rest.dto.*;
import com.predictorama.backend.adapter.rest.mapper.GroupMemberMapper;
import com.predictorama.backend.adapter.rest.mapper.GroupMapper;
import com.predictorama.backend.domain.port.persistence.UserRepositoryPort;
import com.predictorama.backend.domain.service.CompetitionCatalog;
import com.predictorama.backend.domain.service.GroupService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {

    private static final Logger log = LoggerFactory.getLogger(GroupController.class);

    private final GroupService groupService;
    private final UserRepositoryPort userRepository;
    private final CompetitionCatalog competitionCatalog;

    private UUID currentUserId() {
        return UUID.fromString((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    private String resolveMemberName(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> user.getUsername())
                .orElse(userId.toString());
    }

    @PostMapping
    public ResponseEntity<GroupResponseDto> createGroup(@RequestBody CreateGroupRequestDto request, HttpServletRequest httpRequest) {
        UUID userId = currentUserId();
        log.info("POST {} - userId={}, name={}", httpRequest.getRequestURI(), userId, request.getName());
        GroupResponseDto response = GroupMapper.toResponse(groupService.createGroup(userId, request.getName(), request.getDescription()));
        log.info("Group created - id={}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/join")
    public ResponseEntity<GroupMemberResponseDto> joinGroup(@RequestBody JoinGroupRequestDto request, HttpServletRequest httpRequest) {
        UUID userId = currentUserId();
        log.info("POST {} - userId={}, inviteCode={}", httpRequest.getRequestURI(), userId, request.getInviteCode());
        return groupService.joinGroup(userId, request.getInviteCode())
                .map(member -> {
                    log.info("User joined group - memberId={}", member.getId());
                    return ResponseEntity.ok(GroupMemberMapper.toResponse(member));
                })
                .orElseGet(() -> {
                    log.warn("Join failed - invite code not found: {}", request.getInviteCode());
                    return ResponseEntity.notFound().build();
                });
    }

    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<Void> leaveGroup(@PathVariable UUID groupId, HttpServletRequest httpRequest) {
        UUID userId = currentUserId();
        log.info("DELETE {} - groupId={}, userId={}", httpRequest.getRequestURI(), groupId, userId);
        groupService.leaveGroup(userId, groupId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    public ResponseEntity<List<UserGroupsResponseDto>> getMyGroups(HttpServletRequest httpRequest) {
        UUID userId = currentUserId();
        log.info("GET {} - userId={}", httpRequest.getRequestURI(), userId);
        List<UserGroupsResponseDto> response = groupService.getUserGroups(userId).stream()
                .map(GroupMapper::toUserGroupsResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDetailsResponse> getGroupDetails(@PathVariable UUID groupId) {
        UUID userId = currentUserId();
        var details = groupService.getGroupDetails(userId, groupId);
        return ResponseEntity.ok(new GroupDetailsResponse(
                details.getGroup().getId(),
                details.getGroup().getName(),
                details.getGroup().getDescription(),
                details.getCurrentUserRole()
        ));
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMemberResponseDto>> getGroupMembers(@PathVariable UUID groupId) {
        UUID userId = currentUserId();
        return ResponseEntity.ok(groupService.getGroupMembers(userId, groupId).stream()
                .map(memberView -> GroupMemberMapper.toResponse(memberView.member(), memberView.username()))
                .toList());
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<GroupMemberResponseDto> addGroupMember(
            @PathVariable UUID groupId,
            @Valid @RequestBody AddGroupMemberRequest request
    ) {
        UUID userId = currentUserId();
        var member = groupService.addMemberByEmail(userId, groupId, request.getEmail().trim());
        GroupMemberResponseDto response = GroupMemberMapper.toResponse(member, resolveMemberName(member.getUserId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{groupId}/members/{memberUserId}")
    public ResponseEntity<Void> removeGroupMember(
            @PathVariable UUID groupId,
            @PathVariable UUID memberUserId
    ) {
        UUID userId = currentUserId();
        groupService.removeMember(userId, groupId, memberUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{groupId}/tournaments")
    public ResponseEntity<List<GroupTournamentResponse>> getGroupTournaments(@PathVariable UUID groupId) {
        UUID userId = currentUserId();
        return ResponseEntity.ok(
                groupService.getGroupTournaments(userId, groupId).stream()
                        .map(tournament -> new GroupTournamentResponse(
                                tournament.getId(),
                                competitionCatalog.toCompetitionCode(tournament.getName()),
                                tournament.getName(),
                                tournament.getSeasonLabel(),
                                tournament.getSport()
                        ))
                        .toList()
        );
    }

    @GetMapping("/{groupId}/leaderboards")
    public ResponseEntity<List<GroupLeaderboardResponse>> getGroupLeaderboards(@PathVariable UUID groupId) {
        UUID userId = currentUserId();
        return ResponseEntity.ok(
                groupService.getGroupLeaderboards(userId, groupId).stream()
                        .map(leaderboard -> new GroupLeaderboardResponse(
                                leaderboard.tournament().getId(),
                                competitionCatalog.toCompetitionCode(leaderboard.tournament().getName()),
                                leaderboard.tournament().getName(),
                                leaderboard.entries().stream()
                                        .map(entry -> new GroupLeaderboardEntryResponse(
                                                entry.userId(),
                                                entry.username(),
                                                entry.totalScore(),
                                                entry.scoredPredictions(),
                                                entry.totalPredictions()
                                        ))
                                        .toList()
                        ))
                        .toList()
        );
    }

    @PostMapping("/{groupId}/tournaments")
    public ResponseEntity<Void> addGroupTournament(
            @PathVariable UUID groupId,
            @Valid @RequestBody AddGroupTournamentRequest request
    ) {
        UUID userId = currentUserId();
        groupService.addTournamentToGroup(userId, groupId, request.getTournamentId());
        return ResponseEntity.status(HttpStatus.CREATED).<Void>build();
    }

    @DeleteMapping("/{groupId}/tournaments/{tournamentId}")
    public ResponseEntity<Void> removeGroupTournament(
            @PathVariable UUID groupId,
            @PathVariable UUID tournamentId
    ) {
        UUID userId = currentUserId();
        groupService.removeTournamentFromGroup(userId, groupId, tournamentId);
        return ResponseEntity.noContent().build();
    }
}
