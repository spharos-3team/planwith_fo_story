package com.planwith.planwith_fo_story.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.planwith.planwith_fo_story.application.query.StoryFeedView;
import com.planwith.planwith_fo_story.application.query.StoryListView;
import com.planwith.planwith_fo_story.application.query.StorySummaryView;

public record StoryListResponse(
		List<StoryListItem> items,
		int page,
		int size
) {
	public static StoryListResponse from(StoryListView view) {
		return new StoryListResponse(toItems(view.items()), view.page(), view.size());
	}

	public static StoryListResponse from(StoryFeedView view) {
		return new StoryListResponse(toItems(view.items()), view.page(), view.size());
	}

	private static List<StoryListItem> toItems(List<StorySummaryView> items) {
		return items.stream().map(StoryListItem::from).toList();
	}

	public record StoryListItem(
			String storyUuid,
			String coverImageUrl,
			String memberUuid,
			String authorNickname,
			String title,
			List<String> countries,
			List<String> cities,
			LocalDateTime createdAt,
			long storyLikeCount,
			long storyCommentCount,
			long viewCount
	) {
		private static StoryListItem from(StorySummaryView view) {
			return new StoryListItem(
					view.storyUuid(),
					view.coverImageUrl(),
					view.memberUuid(),
					view.author() == null ? null : view.author().nickname(),
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
