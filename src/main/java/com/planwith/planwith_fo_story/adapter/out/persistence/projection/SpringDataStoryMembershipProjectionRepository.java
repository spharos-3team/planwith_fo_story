package com.planwith.planwith_fo_story.adapter.out.persistence.projection;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataStoryMembershipProjectionRepository
		extends JpaRepository<StoryMembershipProjectionJpaEntity, StoryMembershipProjectionJpaEntity.StoryMembershipProjectionId> {
}
