package com.planwith.planwith_fo_story.application.query;

import java.time.LocalDateTime;
import java.util.List;

import com.planwith.planwith_fo_story.domain.model.VisibilityScope;

public record StorySummaryView(
		String storyUuid,
		String memberUuid,
		String title,
		String coverImageUrl,
		List<String> countryNames,
		VisibilityScope visibilityScope,
		long viewCount,
		long storyLikeCount,
		long storyCommentCount,
		LocalDateTime createdAt,
		StoryAuthorView author
) {
}
