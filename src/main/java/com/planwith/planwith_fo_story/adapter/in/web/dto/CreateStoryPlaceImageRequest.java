package com.planwith.planwith_fo_story.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Schema(description = "스토리 장소 이미지 요청")
public record CreateStoryPlaceImageRequest(
		@NotBlank(message = "장소 이미지 URL은 필수입니다.")
		@Size(max = 500, message = "장소 이미지 URL은 500자를 초과할 수 없습니다.")
		String imageUrl,
		@NotNull(message = "장소 이미지 순서는 필수입니다.")
		@Min(value = 1, message = "장소 이미지 순서는 1 이상이어야 합니다.")
		@Max(value = 5, message = "장소 이미지 순서는 5 이하여야 합니다.")
		Integer imageOrder
) {
}
