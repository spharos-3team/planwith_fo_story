package com.planwith.planwith_fo_story.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class StoryCachePropertiesTest {

	@Test
	void buildsNamespacedCacheKeys() {
		StoryCacheProperties properties = new StoryCacheProperties();
		String storyUuid = UUID.randomUUID().toString();
		String memberUuid = UUID.randomUUID().toString();

		assertThat(properties.detailKey(storyUuid)).isEqualTo("story:detail:" + storyUuid);
		assertThat(properties.popularKey()).isEqualTo("story:popular");
		assertThat(properties.feedKey(memberUuid)).isEqualTo("story:feed:" + memberUuid);
		assertThat(properties.resolvedTtl()).isEqualTo(Duration.ofMinutes(10));
	}
}
