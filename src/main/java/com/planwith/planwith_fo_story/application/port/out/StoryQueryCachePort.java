package com.planwith.planwith_fo_story.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_story.application.query.StoryDetailView;
import com.planwith.planwith_fo_story.application.query.StoryFeedView;
import com.planwith.planwith_fo_story.application.query.StoryListView;

public interface StoryQueryCachePort {

	Optional<StoryDetailView> findDetail(UUID storyUuid);

	void saveDetail(UUID storyUuid, StoryDetailView view);

	void evictDetail(UUID storyUuid);

	Optional<StoryListView> findPopular();

	void savePopular(StoryListView view);

	void evictPopular();

	Optional<StoryFeedView> findFeed(UUID memberUuid);

	void saveFeed(UUID memberUuid, StoryFeedView view);

	void evictFeed(UUID memberUuid);
}
