package com.planwith.planwith_fo_story.application.query;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.planwith.planwith_fo_story.domain.model.AiModerationStatus;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;

public record StoryDetailView(
		String storyUuid,
		String memberUuid,
		String scheduleUuid,
		String title,
		String content,
		String coverImageUrl,
		String visitCountry,
		String visitCity,
		String visitPlace,
		LocalDate startDate,
		LocalDate endDate,
		boolean commentEnabled,
		VisibilityScope visibilityScope,
		AiModerationStatus aiModerationStatus,
		long storyLikeCount,
		LocalDateTime createdAt,
		StoryAuthorView author
) {
}
