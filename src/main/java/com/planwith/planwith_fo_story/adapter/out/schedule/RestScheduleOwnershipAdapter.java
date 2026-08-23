package com.planwith.planwith_fo_story.adapter.out.schedule;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.planwith.planwith_fo_story.application.port.out.ScheduleOwnershipPort;
import com.planwith.planwith_fo_story.config.StoryScheduleProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "story.schedule", name = "ownership-check-enabled", havingValue = "true")
public class RestScheduleOwnershipAdapter implements ScheduleOwnershipPort {

	private final RestClient restClient;
	private final StoryScheduleProperties properties;

	public RestScheduleOwnershipAdapter(RestClient.Builder restClientBuilder, StoryScheduleProperties properties) {
		this.properties = properties;
		this.restClient = restClientBuilder.baseUrl(properties.getBaseUrl()).build();
	}

	@Override
	public boolean isOwnedBy(UUID scheduleUuid, UUID memberUuid) {
		log.info(
				"RestScheduleOwnershipAdapter : isOwnedBy : Schedule Service 소유 확인 시작 - scheduleUuid={}",
				scheduleUuid
		);
		try {
			OwnershipResponse response = restClient.get()
					.uri(properties.getOwnershipPath(), scheduleUuid, memberUuid)
					.retrieve()
					.body(OwnershipResponse.class);
			boolean owned = response != null && response.owned();
			log.info(
					"RestScheduleOwnershipAdapter : isOwnedBy : Schedule Service 소유 확인 완료 - scheduleUuid={}, owned={}",
					scheduleUuid,
					owned
			);
			return owned;
		} catch (RestClientException exception) {
			log.warn(
					"RestScheduleOwnershipAdapter : isOwnedBy : Schedule Service 소유 확인 실패 - scheduleUuid={}",
					scheduleUuid
			);
			return false;
		}
	}

	public record OwnershipResponse(boolean owned) {
	}
}
