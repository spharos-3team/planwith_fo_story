package com.planwith.planwith_fo_story.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "스토리 댓글 허용 변경 요청")
public record ChangeStoryCommentEnabledRequest(
		@NotNull(message = "댓글 허용 여부는 필수입니다.")
		Boolean commentEnabled
) {
}
