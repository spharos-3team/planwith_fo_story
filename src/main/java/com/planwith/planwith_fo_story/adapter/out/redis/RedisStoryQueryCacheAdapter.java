package com.planwith.planwith_fo_story.adapter.out.redis;

import java.util.Optional;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_story.application.port.out.StoryQueryCachePort;
import com.planwith.planwith_fo_story.application.query.StoryDetailView;
import com.planwith.planwith_fo_story.application.query.StoryFeedView;
import com.planwith.planwith_fo_story.application.query.StoryListView;
import com.planwith.planwith_fo_story.config.StoryCacheProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile("!test")
@Component
@RequiredArgsConstructor
public class RedisStoryQueryCacheAdapter implements StoryQueryCachePort {

	private final StringRedisTemplate redisTemplate;
	private final ObjectMapper objectMapper;
	private final StoryCacheProperties properties;

	@Override
	public Optional<StoryDetailView> findDetail(UUID storyUuid) {
		return read(properties.detailKey(storyUuid.toString()), StoryDetailView.class, "findDetail");
	}

	@Override
	public void saveDetail(UUID storyUuid, StoryDetailView view) {
		write(properties.detailKey(storyUuid.toString()), view, "saveDetail");
	}

	@Override
	public void evictDetail(UUID storyUuid) {
		evict(properties.detailKey(storyUuid.toString()), "evictDetail");
	}

	@Override
	public Optional<StoryListView> findPopular() {
		return read(properties.popularKey(), StoryListView.class, "findPopular");
	}

	@Override
	public void savePopular(StoryListView view) {
		write(properties.popularKey(), view, "savePopular");
	}

	@Override
	public void evictPopular() {
		evict(properties.popularKey(), "evictPopular");
	}

	@Override
	public Optional<StoryFeedView> findFeed(UUID memberUuid) {
		return read(properties.feedKey(memberUuid.toString()), StoryFeedView.class, "findFeed");
	}

	@Override
	public void saveFeed(UUID memberUuid, StoryFeedView view) {
		write(properties.feedKey(memberUuid.toString()), view, "saveFeed");
	}

	@Override
	public void evictFeed(UUID memberUuid) {
		evict(properties.feedKey(memberUuid.toString()), "evictFeed");
	}

	private <T> Optional<T> read(String key, Class<T> type, String methodName) {
		try {
			String value = redisTemplate.opsForValue().get(key);
			if (value == null || value.isBlank()) {
				log.debug("RedisStoryQueryCacheAdapter : {} : 조회 캐시 MISS - key={}", methodName, key);
				return Optional.empty();
			}
			T view = objectMapper.readValue(value, type);
			log.debug("RedisStoryQueryCacheAdapter : {} : 조회 캐시 HIT - key={}", methodName, key);
			return Optional.of(view);
		} catch (Exception exception) {
			log.warn(
					"RedisStoryQueryCacheAdapter : {} : Redis 장애로 Query DB fallback 가능 상태 유지 - key={}",
					methodName,
					key,
					exception
			);
			return Optional.empty();
		}
	}

	private void write(String key, Object view, String methodName) {
		try {
			redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(view), properties.resolvedTtl());
			log.debug("RedisStoryQueryCacheAdapter : {} : 조회 캐시 저장 완료 - key={}", methodName, key);
		} catch (JsonProcessingException | RuntimeException exception) {
			log.warn("RedisStoryQueryCacheAdapter : {} : Redis 저장 실패 - key={}", methodName, key, exception);
		}
	}

	private void evict(String key, String methodName) {
		try {
			redisTemplate.delete(key);
			log.debug("RedisStoryQueryCacheAdapter : {} : 조회 캐시 삭제 완료 - key={}", methodName, key);
		} catch (RuntimeException exception) {
			log.warn("RedisStoryQueryCacheAdapter : {} : Redis 삭제 실패 - key={}", methodName, key, exception);
		}
	}
}
