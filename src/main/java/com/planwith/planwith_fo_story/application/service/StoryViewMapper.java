package com.planwith.planwith_fo_story.application.service;

import com.planwith.planwith_fo_story.application.query.StoryAuthorView;
import com.planwith.planwith_fo_story.application.query.StoryDetailView;
import com.planwith.planwith_fo_story.application.query.StorySummaryView;
import com.planwith.planwith_fo_story.domain.model.Story;
import com.planwith.planwith_fo_story.domain.model.projection.MemberProfileProjection;

final class StoryViewMapper {

	private StoryViewMapper() {
	}

	static StoryDetailView toDetail(Story story, MemberProfileProjection authorProjection) {
		return new StoryDetailView(
				story.storyUuid().asString(),
				story.memberUuid().asString(),
				story.scheduleUuid() == null ? null : story.scheduleUuid().toString(),
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
				story.createdAt(),
				toAuthor(story, authorProjection)
		);
	}

	static StorySummaryView toSummary(Story story, MemberProfileProjection authorProjection) {
		return new StorySummaryView(
				story.storyUuid().asString(),
				story.memberUuid().asString(),
				story.title(),
				story.coverImageUrl(),
				story.visitCountry(),
				story.visitCity(),
				story.visibilityScope(),
				story.storyLikeCount(),
				story.createdAt(),
				toAuthor(story, authorProjection)
		);
	}

	private static StoryAuthorView toAuthor(Story story, MemberProfileProjection authorProjection) {
		if (authorProjection == null) {
			return new StoryAuthorView(story.memberUuid().asString(), null, null);
		}
		return new StoryAuthorView(
				authorProjection.memberUuid().asString(),
				authorProjection.nickname(),
				authorProjection.profileImage()
		);
	}
}
