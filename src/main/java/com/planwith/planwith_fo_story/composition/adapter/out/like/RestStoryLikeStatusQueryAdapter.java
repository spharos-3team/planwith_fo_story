package com.planwith.planwith_fo_story.composition.adapter.out.like;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.planwith.planwith_fo_story.composition.application.port.out.StoryLikeStatusQueryPort;
import com.planwith.planwith_fo_story.composition.config.StoryDetailScreenProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "story.detail-screen.like", name = "query-enabled", havingValue = "true")
public class RestStoryLikeStatusQueryAdapter implements StoryLikeStatusQueryPort {

	private final RestClient restClient;
	private final StoryDetailScreenProperties properties;

	public RestStoryLikeStatusQueryAdapter(RestClient.Builder restClientBuilder, StoryDetailScreenProperties properties) {
		this.properties = properties;
		this.restClient = restClientBuilder.baseUrl(properties.getLike().getBaseUrl()).build();
	}

	@Override
	public boolean isLikedByViewer(UUID storyUuid, UUID viewerUuid) {
		log.info(
				"RestStoryLikeStatusQueryAdapter : isLikedByViewer : Like Service 좋아요 여부 조회 시작 - storyUuid={}",
				storyUuid
		);
		try {
			LikeStatusResponse response = restClient.get()
					.uri(properties.getLike().getStatusPath(), storyUuid)
					.header("X-Member-UUID", viewerUuid.toString())
					.retrieve()
					.body(LikeStatusResponse.class);
			boolean liked = response != null && response.liked();
			log.info(
					"RestStoryLikeStatusQueryAdapter : isLikedByViewer : Like Service 좋아요 여부 조회 완료 - storyUuid={}, liked={}",
					storyUuid,
					liked
			);
			return liked;
		} catch (RestClientException exception) {
			log.warn(
					"RestStoryLikeStatusQueryAdapter : isLikedByViewer : Like Service 좋아요 여부 조회 실패 - storyUuid={}",
					storyUuid
			);
			return false;
		}
	}

	public record LikeStatusResponse(boolean liked) {
	}
}
