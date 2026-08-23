package com.planwith.planwith_fo_story.application.query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.planwith.planwith_fo_story.domain.model.AiModerationStatus;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;

public record StoryDetailView(
		String storyUuid,
		String memberUuid,
		String scheduleUuid,
		boolean scheduleVisible,
		String title,
		String content,
		String coverImageUrl,
		LocalDate startDate,
		LocalDate endDate,
		boolean commentEnabled,
		VisibilityScope visibilityScope,
		AiModerationStatus aiModerationStatus,
		long viewCount,
		long storyLikeCount,
		long storyCommentCount,
		LocalDateTime createdAt,
		LocalDateTime updatedAt,
		List<StoryVisitCountryView> visitCountries,
		List<StoryPlaceView> places,
		List<String> tags,
		List<String> visibilityMemberUuids,
		StoryAuthorView author
) {
}
