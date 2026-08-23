package com.planwith.planwith_fo_story.adapter.out.persistence.story;

import com.planwith.planwith_fo_story.domain.model.Story;
import com.planwith.planwith_fo_story.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_story.domain.model.vo.StoryUuid;

final class StoryPersistenceMapper {

	private StoryPersistenceMapper() {
	}

	static Story toDomain(StoryJpaEntity entity) {
		return Story.restore(
				entity.storyId(),
				StoryUuid.of(entity.storyUuid()),
				MemberUuid.of(entity.memberUuid()),
				entity.scheduleUuid(),
				entity.title(),
				entity.content(),
				entity.coverImageUrl(),
				entity.visitCountry(),
				entity.visitCity(),
				entity.visitPlace(),
				entity.startDate(),
				entity.endDate(),
				entity.commentEnabled(),
				entity.visibilityScope(),
				entity.aiModerationStatus(),
				entity.storyLikeCount(),
				entity.createdAt(),
				entity.deletedAt()
		);
	}

	static void apply(Story story, StoryJpaEntity entity) {
		if (entity.storyUuid() == null) {
			entity.assignIdentity(story.storyUuid().value(), story.memberUuid().value(), story.createdAt());
		}
		entity.apply(
				story.scheduleUuid(),
				story.title(),
				story.content(),
				story.coverImageUrl(),
				story.visitCountry(),
				story.visitCity(),
				story.visitPlace(),
				story.startDate(),
				story.endDate(),
				story.commentEnabled(),
				story.visibilityScope(),
				story.aiModerationStatus(),
				story.storyLikeCount(),
				story.deletedAt()
		);
	}
}
