package com.planwith.planwith_fo_story.domain.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import com.planwith.planwith_fo_story.domain.exception.InvalidStoryStateException;
import com.planwith.planwith_fo_story.domain.exception.MemberAuthenticationRequiredException;
import com.planwith.planwith_fo_story.domain.model.StoryPlace;
import com.planwith.planwith_fo_story.domain.model.StoryPlaceImage;
import com.planwith.planwith_fo_story.domain.model.StoryTag;
import com.planwith.planwith_fo_story.domain.model.StoryVisibilityMember;
import com.planwith.planwith_fo_story.domain.model.StoryVisitCity;
import com.planwith.planwith_fo_story.domain.model.StoryVisitCountry;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;
import com.planwith.planwith_fo_story.domain.model.vo.MemberUuid;

public class StoryCommonValidator {

	private static final int MAX_PLACE_IMAGES = 5;
	private static final Pattern VIDEO_URL_PATTERN = Pattern.compile(
			".*\\.(mp4|mov|avi|wmv|webm|mkv|m4v)(\\?.*)?$",
			Pattern.CASE_INSENSITIVE
	);

	public void validateAuthor(MemberUuid author) {
		if (author == null) {
			throw new MemberAuthenticationRequiredException();
		}
	}

	public void validateWrite(StoryWriteSpec spec) {
		validateBody(
				spec.scheduleUuid(),
				spec.scheduleVisible(),
				spec.coverImageUrl(),
				spec.startDate(),
				spec.endDate(),
				spec.visibilityScope()
		);
		validateRequiredText(spec.title(), "제목");
		validateRequiredText(spec.content(), "내용");
		validateChildren(
				spec.visibilityScope(),
				spec.visitCountries(),
				spec.places(),
				spec.tags(),
				spec.visibilityMembers()
		);
	}

	public void validateBody(
			UUID scheduleUuid,
			boolean scheduleVisible,
			String coverImageUrl,
			LocalDate startDate,
			LocalDate endDate,
			VisibilityScope visibilityScope
	) {
		if (visibilityScope == null) {
			throw new InvalidStoryStateException("공개범위는 필수입니다.");
		}
		if (startDate == null || endDate == null) {
			throw new InvalidStoryStateException("여행기간은 필수입니다.");
		}
		if (endDate.isBefore(startDate)) {
			throw new InvalidStoryStateException("여행 종료일은 시작일보다 빠를 수 없습니다.");
		}
		validateScheduleShare(scheduleUuid, scheduleVisible);
		rejectVideoUrl(coverImageUrl, "대표사진");
	}

	public void validateChildren(
			VisibilityScope visibilityScope,
			List<StoryVisitCountry> visitCountries,
			List<StoryPlace> places,
			List<StoryTag> tags,
			List<StoryVisibilityMember> visibilityMembers
	) {
		validateVisitCountries(visitCountries);
		validatePlaces(places);
		validateTags(tags);
		validateVisibilityMembers(visibilityScope, visibilityMembers);
	}

	public void validateScheduleShare(UUID scheduleUuid, boolean scheduleVisible) {
		if (scheduleVisible && scheduleUuid == null) {
			throw new InvalidStoryStateException("일정 공유를 사용하려면 일정 UUID가 필요합니다.");
		}
	}

	public void validateVisibilityMembers(
			VisibilityScope visibilityScope,
			List<StoryVisibilityMember> visibilityMembers
	) {
		List<StoryVisibilityMember> members = emptyIfNull(visibilityMembers);
		if (visibilityScope == VisibilityScope.PRIVATE && members.isEmpty()) {
			throw new InvalidStoryStateException("비공개 스토리는 지정 회원 목록이 필요합니다.");
		}
		if (visibilityScope != VisibilityScope.PRIVATE && !members.isEmpty()) {
			throw new InvalidStoryStateException("지정 회원 목록은 비공개 스토리에서만 사용할 수 있습니다.");
		}
		Set<UUID> distinctMembers = new HashSet<>();
		for (StoryVisibilityMember member : members) {
			if (!distinctMembers.add(member.memberUuid().value())) {
				throw new InvalidStoryStateException("지정 회원은 중복될 수 없습니다.");
			}
		}
	}

	public void rejectVideoUrl(String url, String fieldName) {
		if (url != null && VIDEO_URL_PATTERN.matcher(url.trim()).matches()) {
			throw new InvalidStoryStateException(fieldName + "에는 동영상을 사용할 수 없습니다.");
		}
	}

	private void validateVisitCountries(List<StoryVisitCountry> visitCountries) {
		List<StoryVisitCountry> countries = emptyIfNull(visitCountries);
		if (countries.isEmpty()) {
			throw new InvalidStoryStateException("방문국가는 최소 1개 이상이어야 합니다.");
		}
		Set<String> distinctCountries = new HashSet<>();
		int cityCount = 0;
		for (StoryVisitCountry country : countries) {
			String countryKey = normalizeName(country.countryName());
			if (!distinctCountries.add(countryKey)) {
				throw new InvalidStoryStateException("동일 스토리 내부에서 방문국가는 중복될 수 없습니다.");
			}
			cityCount += validateVisitCities(country.cities());
		}
		if (cityCount < 1) {
			throw new InvalidStoryStateException("방문도시는 최소 1개 이상이어야 합니다.");
		}
	}

	private int validateVisitCities(List<StoryVisitCity> cities) {
		List<StoryVisitCity> cityList = emptyIfNull(cities);
		if (cityList.isEmpty()) {
			throw new InvalidStoryStateException("방문도시는 최소 1개 이상이어야 합니다.");
		}
		Set<String> distinctCities = new HashSet<>();
		for (StoryVisitCity city : cityList) {
			if (!distinctCities.add(normalizeName(city.cityName()))) {
				throw new InvalidStoryStateException("동일 국가 내부에서 방문도시는 중복될 수 없습니다.");
			}
		}
		return cityList.size();
	}

	private void validatePlaces(List<StoryPlace> places) {
		for (StoryPlace place : emptyIfNull(places)) {
			List<StoryPlaceImage> images = emptyIfNull(place.images());
			if (images.size() > MAX_PLACE_IMAGES) {
				throw new InvalidStoryStateException("장소 이미지는 장소당 최대 5개까지 허용됩니다.");
			}
			for (StoryPlaceImage image : images) {
				rejectVideoUrl(image.imageUrl(), "장소 이미지");
			}
		}
	}

	private void validateTags(List<StoryTag> tags) {
		Set<String> distinctTags = new HashSet<>();
		for (StoryTag tag : emptyIfNull(tags)) {
			if (!distinctTags.add(normalizeName(tag.tagName()))) {
				throw new InvalidStoryStateException("동일 스토리 내부에서 태그는 중복될 수 없습니다.");
			}
		}
	}

	private void validateRequiredText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new InvalidStoryStateException(fieldName + "은 필수입니다.");
		}
	}

	private static String normalizeName(String value) {
		return value.trim().toLowerCase(Locale.ROOT);
	}

	private static <T> List<T> emptyIfNull(List<T> values) {
		return values == null ? List.of() : values;
	}
}
