package com.planwith.planwith_fo_story.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.planwith.planwith_fo_story.application.query.StoryListView;
import com.planwith.planwith_fo_story.application.query.StorySummaryView;

public record MyStoryListResponse(
		List<MyStoryListItem> items,
		int page,
		int size
) {
	public static MyStoryListResponse from(StoryListView view) {
		return new MyStoryListResponse(
				view.items().stream().map(MyStoryListItem::from).toList(),
				view.page(),
				view.size()
		);
	}

	public record MyStoryListItem(
			String storyUuid,
			String coverImageUrl,
			String title,
			List<String> countries,
			List<String> cities,
			LocalDateTime createdAt,
			long storyLikeCount,
			long storyCommentCount,
			long viewCount
	) {
		private static MyStoryListItem from(StorySummaryView view) {
			return new MyStoryListItem(
					view.storyUuid(),
					view.coverImageUrl(),
					view.title(),
					view.countryNames(),
					view.cityNames(),
					view.createdAt(),
					view.storyLikeCount(),
					view.storyCommentCount(),
					view.viewCount()
			);
		}
	}
}
