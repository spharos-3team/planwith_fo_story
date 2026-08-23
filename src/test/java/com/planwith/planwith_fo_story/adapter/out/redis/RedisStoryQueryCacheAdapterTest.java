package com.planwith.planwith_fo_story.adapter.out.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.planwith.planwith_fo_story.application.query.StoryAuthorView;
import com.planwith.planwith_fo_story.application.query.StoryDetailView;
import com.planwith.planwith_fo_story.application.query.StoryFeedView;
import com.planwith.planwith_fo_story.application.query.StorySummaryView;
import com.planwith.planwith_fo_story.config.StoryCacheProperties;
import com.planwith.planwith_fo_story.domain.model.AiModerationStatus;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;

class RedisStoryQueryCacheAdapterTest {

	private final ObjectMapper objectMapper = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	private final StoryCacheProperties properties = new StoryCacheProperties();
	private final UUID storyUuid = UUID.randomUUID();

	private StringRedisTemplate redisTemplate;
	private ValueOperations<String, String> valueOperations;
	private RedisStoryQueryCacheAdapter adapter;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void setUp() {
		redisTemplate = mock(StringRedisTemplate.class);
		valueOperations = mock(ValueOperations.class);
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		adapter = new RedisStoryQueryCacheAdapter(redisTemplate, objectMapper, properties);
	}

	@Test
	void returnsCachedDetailOnHit() throws Exception {
		StoryDetailView view = detailView();
		when(valueOperations.get(properties.detailKey(storyUuid.toString())))
				.thenReturn(objectMapper.writeValueAsString(view));

		assertThat(adapter.findDetail(storyUuid)).contains(view);
	}

	@Test
	void returnsEmptyOnMiss() {
		when(valueOperations.get(properties.detailKey(storyUuid.toString()))).thenReturn(null);

		assertThat(adapter.findDetail(storyUuid)).isEmpty();
	}

	@Test
	void returnsEmptyWhenRedisReadFailsSoQueryDbCanBeUsed() {
		when(valueOperations.get(properties.detailKey(storyUuid.toString())))
				.thenThrow(new RuntimeException("Redis unavailable"));

		assertThat(adapter.findDetail(storyUuid)).isEmpty();
	}

	@Test
	void savesDetailWithConfiguredTtl() throws Exception {
		StoryDetailView view = detailView();

		adapter.saveDetail(storyUuid, view);

		verify(valueOperations).set(
				eq(properties.detailKey(storyUuid.toString())),
				eq(objectMapper.writeValueAsString(view)),
				eq(Duration.ofMinutes(10))
		);
	}

	@Test
	void doesNotThrowWhenRedisSaveFails() {
		doThrow(new RuntimeException("Redis unavailable"))
				.when(valueOperations)
				.set(anyString(), anyString(), any(Duration.class));

		adapter.saveDetail(storyUuid, detailView());
	}

	@Test
	void evictsFeedKey() {
		UUID memberUuid = UUID.randomUUID();

		adapter.evictFeed(memberUuid);

		verify(redisTemplate).delete(properties.feedKey(memberUuid.toString()));
	}

	@Test
	void returnsEmptyFeedWhenRedisFails() {
		UUID memberUuid = UUID.randomUUID();
		when(valueOperations.get(properties.feedKey(memberUuid.toString())))
				.thenThrow(new RuntimeException("Redis unavailable"));

		assertThat(adapter.findFeed(memberUuid)).isEmpty();
		assertThat(adapter.findPopular()).isEmpty();
	}

	private StoryDetailView detailView() {
		return new StoryDetailView(
				storyUuid.toString(),
				UUID.randomUUID().toString(),
				null,
				false,
				"제목",
				"본문",
				"https://img.example/cover.png",
				java.time.LocalDate.of(2026, 8, 1),
				java.time.LocalDate.of(2026, 8, 5),
				true,
				VisibilityScope.ALL,
				AiModerationStatus.UNVERIFIED,
				0L,
				0L,
				0L,
				LocalDateTime.of(2026, 8, 23, 11, 0),
				LocalDateTime.of(2026, 8, 23, 11, 0),
				List.of(),
				List.of(),
				List.of(),
				List.of(),
				new StoryAuthorView(UUID.randomUUID().toString(), "닉네임", null)
		);
	}

	@SuppressWarnings("unused")
	private StoryFeedView feedView() {
		return new StoryFeedView(List.of(new StorySummaryView(
				storyUuid.toString(),
				UUID.randomUUID().toString(),
				"제목",
				"https://img.example/cover.png",
				List.of(),
				VisibilityScope.ALL,
				0L,
				0L,
				0L,
				LocalDateTime.of(2026, 8, 23, 11, 0),
				null
		)), 0, 20);
	}
}
