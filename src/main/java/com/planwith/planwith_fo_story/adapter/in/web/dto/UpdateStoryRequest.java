package com.planwith.planwith_fo_story.adapter.in.web.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import com.planwith.planwith_fo_story.application.command.CreateStoryCommand;
import com.planwith.planwith_fo_story.application.command.UpdateStoryCommand;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "스토리 수정 요청")
public record UpdateStoryRequest(
		@NotBlank(message = "제목은 필수입니다.") @Size(max = 200) String title,
		@NotBlank(message = "본문은 필수입니다.") String content,
		@NotBlank(message = "커버 이미지 URL은 필수입니다.") @Size(max = 500) String coverImageUrl,
		@NotNull(message = "여행 시작일은 필수입니다.") LocalDate startDate,
		@NotNull(message = "여행 종료일은 필수입니다.") LocalDate endDate,
		@NotNull(message = "댓글 허용 여부는 필수입니다.") Boolean commentEnabled,
		@NotNull(message = "공개범위는 필수입니다.") VisibilityScope visibilityScope,
		UUID scheduleUuid,
		Boolean scheduleVisible,
		Boolean aiVerificationRequested,
		@NotEmpty(message = "방문국가는 최소 1개 이상이어야 합니다.") @Valid List<CreateStoryCountryRequest> countries,
		List<@Size(max = 50) String> tags,
		List<UUID> visibilityMemberUuids
) {
	public UpdateStoryCommand toCommand(UUID actorUuid, UUID storyUuid) {
		return new UpdateStoryCommand(
				actorUuid, storyUuid, scheduleUuid, Boolean.TRUE.equals(scheduleVisible),
				title, content, coverImageUrl, startDate, endDate,
				commentEnabled, visibilityScope, Boolean.TRUE.equals(aiVerificationRequested),
				toCountries(), tags == null ? List.of() : List.copyOf(tags),
				visibilityMemberUuids == null ? List.of() : List.copyOf(visibilityMemberUuids)
		);
	}

	private List<CreateStoryCommand.Country> toCountries() {
		return IntStream.range(0, countries.size()).mapToObj(index -> {
			CreateStoryCountryRequest country = countries.get(index);
			return new CreateStoryCommand.Country(
					country.countryName(), resolveOrder(country.displayOrder(), index), toCities(country.cities())
			);
		}).toList();
	}

	private List<CreateStoryCommand.City> toCities(List<CreateStoryCityRequest> cities) {
		return IntStream.range(0, cities.size()).mapToObj(index -> {
			CreateStoryCityRequest city = cities.get(index);
			return new CreateStoryCommand.City(
					city.cityName(), resolveOrder(city.displayOrder(), index), toPlaces(city.places())
			);
		}).toList();
	}

	private List<CreateStoryCommand.Place> toPlaces(List<CreateStoryPlaceRequest> places) {
		if (places == null) {
			return List.of();
		}
		return IntStream.range(0, places.size()).mapToObj(index -> {
			CreateStoryPlaceRequest place = places.get(index);
			return new CreateStoryCommand.Place(
					place.placeName(),
					resolveOrder(place.displayOrder(), index),
					place.images() == null ? List.of() : place.images().stream()
							.map(image -> new CreateStoryCommand.PlaceImage(image.imageUrl(), image.imageOrder()))
							.toList()
			);
		}).toList();
	}

	private static int resolveOrder(Integer displayOrder, int fallback) {
		return displayOrder == null ? fallback : displayOrder;
	}
}
