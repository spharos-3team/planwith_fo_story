package com.planwith.planwith_fo_story.application.query;

import java.time.LocalDateTime;

import com.planwith.planwith_fo_story.domain.model.VisibilityScope;

public record StorySummaryView(
		String storyUuid,
		String memberUuid,
		String title,
		String coverImageUrl,
		String visitCountry,
		String visitCity,
		VisibilityScope visibilityScope,
		long storyLikeCount,
		LocalDateTime createdAt,
		StoryAuthorView author
) {
}
