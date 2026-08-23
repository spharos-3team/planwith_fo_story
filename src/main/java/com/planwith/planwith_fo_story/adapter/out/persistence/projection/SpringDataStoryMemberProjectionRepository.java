package com.planwith.planwith_fo_story.adapter.out.persistence.projection;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataStoryMemberProjectionRepository extends JpaRepository<StoryMemberProjectionJpaEntity, UUID> {
}
