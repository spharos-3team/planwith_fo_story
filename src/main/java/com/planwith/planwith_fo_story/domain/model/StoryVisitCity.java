package com.planwith.planwith_fo_story.domain.model;

import java.util.List;
import java.util.Objects;

import com.planwith.planwith_fo_story.domain.exception.InvalidStoryStateException;

public final class StoryVisitCity {

	private static final int CITY_NAME_MAX_LENGTH = 100;

	private final Long storyVisitCityId;
	private final String cityName;
	private final int displayOrder;
	private final List<StoryPlace> places;

	private StoryVisitCity(
			Long storyVisitCityId,
			String cityName,
			int displayOrder,
			List<StoryPlace> places
	) {
		this.storyVisitCityId = storyVisitCityId;
		this.cityName = requireName(cityName, CITY_NAME_MAX_LENGTH, "도시명");
		this.displayOrder = Math.max(0, displayOrder);
		this.places = List.copyOf(places == null ? List.of() : places);
	}

	public static StoryVisitCity create(String cityName, int displayOrder) {
		return create(cityName, displayOrder, List.of());
	}

	public static StoryVisitCity create(String cityName, int displayOrder, List<StoryPlace> places) {
		return new StoryVisitCity(null, cityName, displayOrder, places);
	}

	public static StoryVisitCity restore(Long storyVisitCityId, String cityName, int displayOrder) {
		return restore(storyVisitCityId, cityName, displayOrder, List.of());
	}

	public static StoryVisitCity restore(
			Long storyVisitCityId,
			String cityName,
			int displayOrder,
			List<StoryPlace> places
	) {
		return new StoryVisitCity(storyVisitCityId, cityName, displayOrder, places);
	}

	private static String requireName(String value, int maxLength, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new InvalidStoryStateException(fieldName + "은 필수입니다.");
		}
		String trimmed = value.trim();
		if (trimmed.length() > maxLength) {
			throw new InvalidStoryStateException(fieldName + " 길이가 허용 범위를 초과했습니다.");
		}
		return trimmed;
	}

	public Long storyVisitCityId() {
		return storyVisitCityId;
	}

	public String cityName() {
		return cityName;
	}

	public int displayOrder() {
		return displayOrder;
	}

	public List<StoryPlace> places() {
		return places;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof StoryVisitCity that)) {
			return false;
		}
		return Objects.equals(cityName, that.cityName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(cityName);
	}
}
