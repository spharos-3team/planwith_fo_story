package com.planwith.planwith_fo_story.adapter.out.feed;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.planwith.planwith_fo_story.application.port.out.StoryFeedMembershipQueryPort;
import com.planwith.planwith_fo_story.config.StoryFeedProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "story.feed", name = "membership-query-enabled", havingValue = "true")
public class RestStoryFeedMembershipQueryAdapter implements StoryFeedMembershipQueryPort {

	private final RestClient restClient;
	private final StoryFeedProperties properties;

	public RestStoryFeedMembershipQueryAdapter(RestClient.Builder restClientBuilder, StoryFeedProperties properties) {
		this.restClient = restClientBuilder.baseUrl(properties.getMembershipBaseUrl()).build();
		this.properties = properties;
	}

	@Override
	public Set<UUID> findJoinedCreatorUuids(UUID viewerUuid) {
		if (viewerUuid == null) {
			return Set.of();
		}
		try {
			JoinedMembership[] memberships = restClient.get()
					.uri(properties.getJoinedMembershipsPath())
					.header("X-Member-UUID", viewerUuid.toString())
					.retrieve()
					.body(JoinedMembership[].class);
			if (memberships == null) {
				return Set.of();
			}
			return Arrays.stream(memberships)
					.filter(item -> item != null && item.creatorUuid() != null && "ACTIVE".equalsIgnoreCase(item.status()))
					.map(JoinedMembership::creatorUuid)
					.collect(Collectors.toUnmodifiableSet());
		} catch (RestClientException exception) {
			log.warn("RestStoryFeedMembershipQueryAdapter : findJoinedCreatorUuids : Membership Service 조회 실패", exception);
			return Set.of();
		}
	}

	public record JoinedMembership(UUID creatorUuid, String status) {
	}
}
