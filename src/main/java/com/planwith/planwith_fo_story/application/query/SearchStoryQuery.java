package com.planwith.planwith_fo_story.application.query;

import java.util.UUID;

public record SearchStoryQuery(
		StorySearchType type,
		String keyword,
		UUID viewerUuid,
		int page,
		int size
) {
	public SearchStoryQuery {
		if (type == null) {
			throw new IllegalArgumentException("검색 유형은 필수입니다.");
		}
		keyword = keyword == null ? "" : keyword.trim();
		if (keyword.isEmpty()) {
			throw new IllegalArgumentException("검색어는 필수입니다.");
		}
	}

	public int offset() {
		return Math.max(0, page) * resolvedSize();
	}

	public int resolvedSize() {
		return size <= 0 ? 20 : Math.min(size, 100);
	}
}
