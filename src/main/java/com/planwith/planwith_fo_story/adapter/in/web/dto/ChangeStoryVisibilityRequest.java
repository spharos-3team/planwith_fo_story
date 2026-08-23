package com.planwith.planwith_fo_story.adapter.in.web.dto;

import com.planwith.planwith_fo_story.domain.model.VisibilityScope;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "스토리 공개범위 변경 요청")
public record ChangeStoryVisibilityRequest(
		@NotNull(message = "공개범위는 필수입니다.")
		VisibilityScope visibilityScope
) {
}
