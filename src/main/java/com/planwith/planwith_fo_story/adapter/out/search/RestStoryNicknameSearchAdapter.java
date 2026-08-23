package com.planwith.planwith_fo_story.adapter.out.search;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.planwith.planwith_fo_story.application.port.out.StoryNicknameSearchPort;
import com.planwith.planwith_fo_story.config.StorySearchProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "story.search", name = "member-query-enabled", havingValue = "true")
public class RestStoryNicknameSearchAdapter implements StoryNicknameSearchPort {

	private static final int PAGE_SIZE = 50;

	private final RestClient restClient;
	private final StorySearchProperties properties;

	public RestStoryNicknameSearchAdapter(RestClient.Builder restClientBuilder, StorySearchProperties properties) {
		this.restClient = restClientBuilder.baseUrl(properties.getMemberBaseUrl()).build();
		this.properties = properties;
	}

	@Override
	public Set<UUID> findMemberUuidsByNickname(String nickname) {
		try {
			return loadMemberUuids(nickname);
		} catch (RestClientException exception) {
			log.warn("RestStoryNicknameSearchAdapter : findMemberUuidsByNickname : Member Service 조회 실패", exception);
			return Set.of();
		}
	}

	private Set<UUID> loadMemberUuids(String nickname) {
		Set<UUID> memberUuids = new HashSet<>();
		int page = 0;
		int totalPages;
		do {
			MemberApiResponse response = loadPage(nickname, page);
			if (response == null || response.data() == null) {
				break;
			}
			MemberPage data = response.data();
			for (MemberItem member : data.content() == null ? List.<MemberItem>of() : data.content()) {
				if (member != null && member.memberUuid() != null) {
					memberUuids.add(member.memberUuid());
				}
			}
			totalPages = Math.max(0, data.totalPages());
			page++;
		} while (page < totalPages);
		return Set.copyOf(memberUuids);
	}

	private MemberApiResponse loadPage(String nickname, int page) {
		return restClient.get()
				.uri(uriBuilder -> uriBuilder.path(properties.getMemberSearchPath())
						.queryParam("nickname", nickname)
						.queryParam("page", page)
						.queryParam("size", PAGE_SIZE)
						.build())
				.retrieve()
				.body(MemberApiResponse.class);
	}

	public record MemberApiResponse(boolean success, MemberPage data) {
	}

	public record MemberPage(List<MemberItem> content, int page, int size, long totalElements, int totalPages) {
	}

	public record MemberItem(UUID memberUuid) {
	}
}
