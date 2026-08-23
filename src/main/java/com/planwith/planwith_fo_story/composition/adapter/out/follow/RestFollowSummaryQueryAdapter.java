package com.planwith.planwith_fo_story.composition.adapter.out.follow;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.planwith.planwith_fo_story.composition.application.port.out.FollowSummaryQueryPort;
import com.planwith.planwith_fo_story.composition.application.query.StoryDetailScreenView.FollowScreenView;
import com.planwith.planwith_fo_story.composition.config.StoryDetailScreenProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "story.detail-screen.follow", name = "query-enabled", havingValue = "true")
public class RestFollowSummaryQueryAdapter implements FollowSummaryQueryPort {

	private final RestClient restClient;
	private final StoryDetailScreenProperties properties;

	public RestFollowSummaryQueryAdapter(RestClient.Builder restClientBuilder, StoryDetailScreenProperties properties) {
		this.properties = properties;
		this.restClient = restClientBuilder.baseUrl(properties.getFollow().getBaseUrl()).build();
	}

	@Override
	public FollowScreenView findByMemberUuid(UUID memberUuid) {
		log.info("RestFollowSummaryQueryAdapter : findByMemberUuid : Follow Service 요약 조회 시작 - memberUuid={}", memberUuid);
		try {
			FollowSummaryResponse response = restClient.get()
					.uri(properties.getFollow().getSummaryPath(), memberUuid)
					.retrieve()
					.body(FollowSummaryResponse.class);
			if (response == null) {
				return new FollowScreenView(0L, 0L);
			}
			log.info(
					"RestFollowSummaryQueryAdapter : findByMemberUuid : Follow Service 요약 조회 완료 - memberUuid={}",
					memberUuid
			);
			return new FollowScreenView(response.followerCount(), response.followingCount());
		} catch (RestClientException exception) {
			log.warn(
					"RestFollowSummaryQueryAdapter : findByMemberUuid : Follow Service 요약 조회 실패 - memberUuid={}",
					memberUuid
			);
			return new FollowScreenView(0L, 0L);
		}
	}

	public record FollowSummaryResponse(long followerCount, long followingCount) {
	}
}
