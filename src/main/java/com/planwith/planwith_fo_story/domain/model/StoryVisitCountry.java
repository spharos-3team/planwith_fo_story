package com.planwith.planwith_fo_story.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.planwith.planwith_fo_story.domain.exception.InvalidStoryStateException;

public final class StoryVisitCountry {

	private static final int COUNTRY_NAME_MAX_LENGTH = 100;

	private final Long storyVisitCountryId;
	private final String countryName;
	private final int displayOrder;
	private final List<StoryVisitCity> cities;

	private StoryVisitCountry(
			Long storyVisitCountryId,
			String countryName,
			int displayOrder,
			List<StoryVisitCity> cities
	) {
		this.storyVisitCountryId = storyVisitCountryId;
		this.countryName = requireName(countryName);
		this.displayOrder = Math.max(0, displayOrder);
		this.cities = List.copyOf(cities == null ? List.of() : cities);
	}

	public static StoryVisitCountry create(String countryName, int displayOrder, List<StoryVisitCity> cities) {
		return new StoryVisitCountry(null, countryName, displayOrder, cities);
	}

	public static StoryVisitCountry restore(
			Long storyVisitCountryId,
			String countryName,
			int displayOrder,
			List<StoryVisitCity> cities
	) {
		return new StoryVisitCountry(storyVisitCountryId, countryName, displayOrder, cities);
	}

	private static String requireName(String value) {
		if (value == null || value.isBlank()) {
			throw new InvalidStoryStateException("국가명은 필수입니다.");
		}
		String trimmed = value.trim();
		if (trimmed.length() > COUNTRY_NAME_MAX_LENGTH) {
			throw new InvalidStoryStateException("국가명 길이가 허용 범위를 초과했습니다.");
		}
		return trimmed;
	}

	public Long storyVisitCountryId() {
		return storyVisitCountryId;
	}

	public String countryName() {
		return countryName;
	}

	public int displayOrder() {
		return displayOrder;
	}

	public List<StoryVisitCity> cities() {
		return cities;
	}

	public List<StoryVisitCity> mutableCities() {
		return new ArrayList<>(cities);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof StoryVisitCountry that)) {
			return false;
		}
		return Objects.equals(countryName, that.countryName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(countryName);
	}
}
