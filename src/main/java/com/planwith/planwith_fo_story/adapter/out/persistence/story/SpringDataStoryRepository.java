package com.planwith.planwith_fo_story.adapter.out.persistence.story;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataStoryRepository extends JpaRepository<StoryJpaEntity, Long> {

	Optional<StoryJpaEntity> findByStoryUuid(UUID storyUuid);

	Optional<StoryJpaEntity> findByStoryUuidAndDeletedAtIsNull(UUID storyUuid);

	List<StoryJpaEntity> findByMemberUuidAndDeletedAtIsNullOrderByCreatedAtDesc(UUID memberUuid, Pageable pageable);

	List<StoryJpaEntity> findByDeletedAtIsNullOrderByCreatedAtDesc(Pageable pageable);

	List<StoryJpaEntity> findByDeletedAtIsNullOrderByStoryLikeCountDescCreatedAtDesc(Pageable pageable);
}
