package com.planwith.planwith_fo_story.application.query;

import java.util.UUID;

public record GetStoryFeedQuery(
		UUID viewerUuid,
		int page,
		int size,
		StorySortType sort,
		StoryFeedType feedType
) {
	public GetStoryFeedQuery(UUID viewerUuid, int page, int size) {
		this(viewerUuid, page, size, StorySortType.LATEST, StoryFeedType.FOLLOWING);
	}

	public GetStoryFeedQuery {
		sort = sort == null ? StorySortType.LATEST : sort;
		feedType = feedType == null ? StoryFeedType.FOLLOWING : feedType;
	}

	public int offset() {
		return Math.max(0, page) * resolvedSize();
	}

	public int resolvedSize() {
		return size <= 0 ? 20 : Math.min(size, 100);
	}
}
