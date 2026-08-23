package com.planwith.planwith_fo_story.adapter.in.web.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.planwith.planwith_fo_story.application.query.StoryDetailView;
import com.planwith.planwith_fo_story.application.query.StoryPlaceView;
import com.planwith.planwith_fo_story.application.query.StoryVisitCountryView;
import com.planwith.planwith_fo_story.domain.model.AiModerationStatus;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "스토리 상세 조회 응답")
public record StoryDetailResponse(
		String storyUuid,
		String memberUuid,
		String scheduleUuid,
		boolean scheduleVisible,
		String coverImageUrl,
		String title,
		String content,
		List<StoryVisitCountryView> countries,
		List<StoryPlaceView> places,
		LocalDate startDate,
		LocalDate endDate,
		List<String> tags,
		boolean commentEnabled,
		VisibilityScope visibilityScope,
		AiModerationStatus aiModerationStatus,
		long viewCount,
		long storyLikeCount,
		long storyCommentCount,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {
	public static StoryDetailResponse from(StoryDetailView view) {
		return new StoryDetailResponse(
				view.storyUuid(),
				view.memberUuid(),
				view.scheduleUuid(),
				view.scheduleVisible(),
				view.coverImageUrl(),
				view.title(),
				view.content(),
				view.visitCountries(),
				view.places(),
				view.startDate(),
				view.endDate(),
				view.tags(),
				view.commentEnabled(),
				view.visibilityScope(),
				view.aiModerationStatus(),
				view.viewCount(),
				view.storyLikeCount(),
				view.storyCommentCount(),
				view.createdAt(),
				view.updatedAt()
		);
	}
}
