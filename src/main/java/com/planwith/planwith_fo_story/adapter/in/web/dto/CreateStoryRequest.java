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
		UUID scheduleUuid,
		Boolean scheduleVisible,
		@NotBlank(message = "제목은 필수입니다.")
		@Size(max = 200, message = "제목은 200자를 초과할 수 없습니다.")
		String title,
		@NotBlank(message = "본문은 필수입니다.")
		String content,
		@NotBlank(message = "커버 이미지 URL은 필수입니다.")
		@Size(max = 500, message = "커버 이미지 URL은 500자를 초과할 수 없습니다.")
		String coverImageUrl,
		@NotNull(message = "여행 시작일은 필수입니다.")
		LocalDate startDate,
		@NotNull(message = "여행 종료일은 필수입니다.")
		LocalDate endDate,
		@NotNull(message = "댓글 허용 여부는 필수입니다.")
		Boolean commentEnabled,
		@NotNull(message = "공개범위는 필수입니다.")
		VisibilityScope visibilityScope
) {
	public boolean resolvedScheduleVisible() {
		return Boolean.TRUE.equals(scheduleVisible);
	}
}
