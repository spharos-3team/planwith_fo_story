package com.planwith.planwith_fo_story.application.query;

import java.util.List;

public record StoryListView(
		List<StorySummaryView> items,
		int page,
		int size
) {
}
