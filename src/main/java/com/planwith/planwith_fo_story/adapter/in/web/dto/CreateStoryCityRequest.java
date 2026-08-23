package com.planwith.planwith_fo_story.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "스토리 방문도시 요청")
public record CreateStoryCityRequest(
		@NotBlank(message = "도시명은 필수입니다.")
		@Size(max = 100, message = "도시명은 100자를 초과할 수 없습니다.")
		String cityName,
		Integer displayOrder
) {
}
