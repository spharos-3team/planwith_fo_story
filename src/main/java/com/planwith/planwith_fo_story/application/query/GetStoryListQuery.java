package com.planwith.planwith_fo_story.application.query;

import java.util.UUID;

public record GetStoryListQuery(
		UUID authorUuid,
		UUID viewerUuid,
		int page,
		int size
) {
	public int offset() {
		return Math.max(0, page) * resolvedSize();
	}

	public int resolvedSize() {
		return size <= 0 ? 20 : Math.min(size, 100);
	}
}
