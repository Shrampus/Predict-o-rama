package com.predictorama.backend.adapter.persistence.repository;

import com.predictorama.backend.adapter.persistence.entity.GroupMemberEntity;
import com.predictorama.backend.domain.entity.GroupMember;
import com.predictorama.backend.domain.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupMemberJpaRepository extends JpaRepository<GroupMemberEntity, UUID> {

    interface MemberWithUsername {
        UUID getId();

        UUID getUserId();

        UUID getGroupId();

        Role getMemberRole();

        GroupMember.MemberStatus getStatus();

        String getUsername();
    }

    List<GroupMemberEntity> findByGroupId(UUID groupId);

    @Query("""
            select gm.id as id, gm.userId as userId, gm.groupId as groupId,
                   gm.memberRole as memberRole, gm.status as status, u.username as username
            from GroupMemberEntity gm join UserEntity u on u.id = gm.userId
            where gm.groupId = :groupId
            """)
    List<MemberWithUsername> findByGroupIdWithUsernames(@Param("groupId") UUID groupId);

    List<GroupMemberEntity> findByUserId(UUID userId);

    Optional<GroupMemberEntity> findByGroupIdAndUserId(UUID groupId, UUID userId);

    @Transactional
    void deleteByGroupIdAndUserId(UUID groupId, UUID userId);
}
