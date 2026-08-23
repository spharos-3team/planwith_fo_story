package com.planwith.planwith_fo_story.adapter.out.feed;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.planwith.planwith_fo_story.application.port.out.StoryFeedMemberQueryPort;
import com.planwith.planwith_fo_story.config.StoryFeedProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "story.feed", name = "member-query-enabled", havingValue = "true")
public class RestStoryFeedMemberQueryAdapter implements StoryFeedMemberQueryPort {

	private static final int PAGE_SIZE = 50;

	private final RestClient restClient;
	private final StoryFeedProperties properties;
	private final Set<String> eligibleGrades;

	public RestStoryFeedMemberQueryAdapter(RestClient.Builder restClientBuilder, StoryFeedProperties properties) {
		this.restClient = restClientBuilder.baseUrl(properties.getMemberBaseUrl()).build();
		this.properties = properties;
		this.eligibleGrades = properties.getEligibleGrades().stream()
				.map(grade -> grade.toUpperCase(Locale.ROOT))
				.collect(java.util.stream.Collectors.toUnmodifiableSet());
	}

	@Override
	public Optional<Set<UUID>> findEligibleFollowingAuthors(UUID viewerUuid) {
		try {
			List<MemberItem> following = viewerUuid == null ? List.of() : loadMembers(properties.getFollowingsPath(), viewerUuid);
			List<MemberItem> candidates = following.isEmpty()
					? loadMembers(properties.getMembersPath(), null)
					: following;
			return Optional.of(toEligibleAuthorUuids(candidates));
		} catch (RestClientException exception) {
			log.warn("RestStoryFeedMemberQueryAdapter : findEligibleFollowingAuthors : Member Service 조회 실패", exception);
			return Optional.empty();
		}
	}

	@Override
	public Optional<Set<UUID>> filterEligibleAuthors(Set<UUID> candidateAuthorUuids) {
		if (candidateAuthorUuids.isEmpty()) {
			return Optional.of(Set.of());
		}
		try {
			Set<UUID> eligibleAuthors = toEligibleAuthorUuids(loadMembers(properties.getMembersPath(), null));
			eligibleAuthors.retainAll(candidateAuthorUuids);
			return Optional.of(Set.copyOf(eligibleAuthors));
		} catch (RestClientException exception) {
			log.warn("RestStoryFeedMemberQueryAdapter : filterEligibleAuthors : Member Service 조회 실패", exception);
			return Optional.empty();
		}
	}

	private List<MemberItem> loadMembers(String path, UUID memberUuid) {
		List<MemberItem> members = new java.util.ArrayList<>();
		int page = 0;
		int totalPages;
		do {
			MemberApiResponse response = loadPage(path, memberUuid, page);
			if (response == null || response.data() == null) {
				break;
			}
			MemberPage data = response.data();
			members.addAll(data.content() == null ? List.of() : data.content());
			totalPages = Math.max(0, data.totalPages());
			page++;
		} while (page < totalPages);
		return List.copyOf(members);
	}

	private MemberApiResponse loadPage(String path, UUID memberUuid, int page) {
		return restClient.get()
				.uri(uriBuilder -> {
					var builder = uriBuilder.path(path)
							.queryParam("page", page)
							.queryParam("size", PAGE_SIZE);
					return memberUuid == null ? builder.build() : builder.build(memberUuid);
				})
				.retrieve()
				.body(MemberApiResponse.class);
	}

	private Set<UUID> toEligibleAuthorUuids(List<MemberItem> members) {
		Set<UUID> result = new HashSet<>();
		for (MemberItem member : members) {
			if (member != null && member.memberUuid() != null && isEligibleGrade(member.grade())) {
				result.add(member.memberUuid());
			}
		}
		return result;
	}

	private boolean isEligibleGrade(String grade) {
		return grade != null && eligibleGrades.contains(grade.toUpperCase(Locale.ROOT));
	}

	public record MemberApiResponse(boolean success, MemberPage data) {
	}

	public record MemberPage(List<MemberItem> content, int page, int size, long totalElements, int totalPages) {
	}

	public record MemberItem(UUID memberUuid, String grade) {
	}
}
