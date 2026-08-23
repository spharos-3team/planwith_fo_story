package com.planwith.planwith_fo_story.adapter.out.redis;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_story.application.port.out.StoryQueryCachePort;
import com.planwith.planwith_fo_story.application.query.StoryDetailView;
import com.planwith.planwith_fo_story.application.query.StoryFeedView;
import com.planwith.planwith_fo_story.application.query.StoryListView;

@Profile("test")
@Component
public class InMemoryStoryQueryCacheAdapter implements StoryQueryCachePort {

	private final Map<String, StoryDetailView> details = new ConcurrentHashMap<>();
	private final Map<String, StoryFeedView> feeds = new ConcurrentHashMap<>();
	private StoryListView popular;

	@Override
	public Optional<StoryDetailView> findDetail(UUID storyUuid) {
		return Optional.ofNullable(details.get(storyUuid.toString()));
	}

	@Override
	public void saveDetail(UUID storyUuid, StoryDetailView view) {
		details.put(storyUuid.toString(), view);
	}

	@Override
	public void evictDetail(UUID storyUuid) {
		details.remove(storyUuid.toString());
	}

	@Override
	public Optional<StoryListView> findPopular() {
		return Optional.ofNullable(popular);
	}

	@Override
	public void savePopular(StoryListView view) {
		this.popular = view;
	}

	@Override
	public void evictPopular() {
		this.popular = null;
	}

	@Override
	public Optional<StoryFeedView> findFeed(UUID memberUuid) {
		return Optional.ofNullable(feeds.get(memberUuid.toString()));
	}

	@Override
	public void saveFeed(UUID memberUuid, StoryFeedView view) {
		feeds.put(memberUuid.toString(), view);
	}

	@Override
	public void evictFeed(UUID memberUuid) {
		feeds.remove(memberUuid.toString());
	}
}
