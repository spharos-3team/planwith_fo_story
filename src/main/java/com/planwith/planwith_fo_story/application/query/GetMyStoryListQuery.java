package com.planwith.planwith_fo_story.application.query;

import java.time.LocalDate;
import java.util.UUID;

import com.planwith.planwith_fo_story.domain.model.VisibilityScope;
import com.planwith.planwith_fo_story.domain.exception.InvalidStoryQueryException;

public record GetMyStoryListQuery(
		UUID memberUuid,
		String country,
		String city,
		VisibilityScope visibilityScope,
		LocalDate travelStartDate,
		LocalDate travelEndDate,
		int page,
		int size
) {
	public GetMyStoryListQuery {
		if (memberUuid == null) {
			throw new IllegalArgumentException("회원 UUID는 필수입니다.");
		}
		country = normalize(country);
		city = normalize(city);
		if (travelStartDate != null && travelEndDate != null && travelStartDate.isAfter(travelEndDate)) {
			throw new InvalidStoryQueryException("여행기간 시작일은 종료일보다 늦을 수 없습니다.");
		}
	}

	public int offset() {
		return Math.max(0, page) * resolvedSize();
	}

	public int resolvedSize() {
		return size <= 0 ? 20 : Math.min(size, 100);
	}

	private static String normalize(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}
