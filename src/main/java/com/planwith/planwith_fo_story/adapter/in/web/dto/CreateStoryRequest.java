package com.planwith.planwith_fo_story.adapter.in.web.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.planwith.planwith_fo_story.domain.model.VisibilityScope;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "스토리 생성 요청")
public record CreateStoryRequest(
		@NotNull(message = "회원 UUID는 필수입니다.")
		UUID memberUuid,
		UUID scheduleUuid,
		@NotBlank(message = "제목은 필수입니다.")
		@Size(max = 200, message = "제목은 200자를 초과할 수 없습니다.")
		String title,
		@NotBlank(message = "본문은 필수입니다.")
		String content,
		@Size(max = 500, message = "커버 이미지 URL은 500자를 초과할 수 없습니다.")
		String coverImageUrl,
		@Size(max = 100, message = "방문 국가는 100자를 초과할 수 없습니다.")
		String visitCountry,
		@Size(max = 100, message = "방문 도시는 100자를 초과할 수 없습니다.")
		String visitCity,
		@Size(max = 255, message = "방문 장소는 255자를 초과할 수 없습니다.")
		String visitPlace,
		LocalDate startDate,
		LocalDate endDate,
		Boolean commentEnabled,
		VisibilityScope visibilityScope
) {
	public boolean resolvedCommentEnabled() {
		return commentEnabled == null || commentEnabled;
	}

	public VisibilityScope resolvedVisibilityScope() {
		return visibilityScope == null ? VisibilityScope.ALL : visibilityScope;
	}
}
