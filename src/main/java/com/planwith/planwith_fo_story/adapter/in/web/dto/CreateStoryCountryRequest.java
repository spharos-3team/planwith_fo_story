package com.planwith.planwith_fo_story.adapter.in.web.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Schema(description = "스토리 방문국가 요청")
public record CreateStoryCountryRequest(
		@NotBlank(message = "국가명은 필수입니다.")
		@Size(max = 100, message = "국가명은 100자를 초과할 수 없습니다.")
		String countryName,
		Integer displayOrder,
		@NotEmpty(message = "방문도시는 최소 1개 이상이어야 합니다.")
		@Valid
		List<CreateStoryCityRequest> cities
) {
}
