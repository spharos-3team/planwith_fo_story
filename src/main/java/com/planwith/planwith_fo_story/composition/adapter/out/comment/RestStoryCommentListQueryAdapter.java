package com.planwith.planwith_fo_story.composition.adapter.out.comment;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.planwith.planwith_fo_story.composition.application.port.out.StoryCommentListQueryPort;
import com.planwith.planwith_fo_story.composition.application.query.StoryCommentItemView;
import com.planwith.planwith_fo_story.composition.config.StoryDetailScreenProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "story.detail-screen.comment", name = "query-enabled", havingValue = "true")
public class RestStoryCommentListQueryAdapter implements StoryCommentListQueryPort {

	private final RestClient restClient;
	private final StoryDetailScreenProperties properties;

	public RestStoryCommentListQueryAdapter(RestClient.Builder restClientBuilder, StoryDetailScreenProperties properties) {
		this.properties = properties;
		this.restClient = restClientBuilder.baseUrl(properties.getComment().getBaseUrl()).build();
	}

	@Override
	public List<StoryCommentItemView> findByStoryUuid(UUID storyUuid) {
		log.info(
				"RestStoryCommentListQueryAdapter : findByStoryUuid : Comment Service 댓글 목록 조회 시작 - storyUuid={}",
				storyUuid
		);
		try {
			CommentListResponse response = restClient.get()
					.uri(properties.getComment().getListPath(), storyUuid)
					.retrieve()
					.body(CommentListResponse.class);
			if (response == null || response.items() == null) {
				return Collections.emptyList();
			}
			List<StoryCommentItemView> items = response.items().stream()
					.map(item -> new StoryCommentItemView(
							item.commentUuid(),
							item.memberUuid(),
							item.content(),
							item.createdAt()
					))
					.toList();
			log.info(
					"RestStoryCommentListQueryAdapter : findByStoryUuid : Comment Service 댓글 목록 조회 완료 - storyUuid={}, count={}",
					storyUuid,
					items.size()
			);
			return items;
		} catch (RestClientException exception) {
			log.warn(
					"RestStoryCommentListQueryAdapter : findByStoryUuid : Comment Service 댓글 목록 조회 실패 - storyUuid={}",
					storyUuid
			);
			return Collections.emptyList();
		}
	}

	public record CommentListResponse(List<CommentItemResponse> items) {
	}

	public record CommentItemResponse(
			String commentUuid,
			String memberUuid,
			String content,
			LocalDateTime createdAt
	) {
	}
}
