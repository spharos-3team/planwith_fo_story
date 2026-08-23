package com.planwith.planwith_fo_story.adapter.in.web.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "스토리 장소 요청")
public record CreateStoryPlaceRequest(
		@NotBlank(message = "장소명은 필수입니다.")
		@Size(max = 255, message = "장소명은 255자를 초과할 수 없습니다.")
		String placeName,
		Integer displayOrder,
		@Valid
		@Size(max = 5, message = "장소 이미지는 장소당 최대 5개까지 허용됩니다.")
		List<CreateStoryPlaceImageRequest> images
) {
}
