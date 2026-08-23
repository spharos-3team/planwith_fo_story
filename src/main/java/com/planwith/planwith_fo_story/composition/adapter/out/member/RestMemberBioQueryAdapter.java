package com.planwith.planwith_fo_story.composition.adapter.out.member;

import java.util.Optional;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.planwith.planwith_fo_story.composition.application.port.out.MemberBioQueryPort;
import com.planwith.planwith_fo_story.composition.config.StoryDetailScreenProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "story.detail-screen.member", name = "query-enabled", havingValue = "true")
public class RestMemberBioQueryAdapter implements MemberBioQueryPort {

	private final RestClient restClient;
	private final StoryDetailScreenProperties properties;

	public RestMemberBioQueryAdapter(RestClient.Builder restClientBuilder, StoryDetailScreenProperties properties) {
		this.properties = properties;
		this.restClient = restClientBuilder.baseUrl(properties.getMember().getBaseUrl()).build();
	}

	@Override
	public Optional<String> findBioByMemberUuid(UUID memberUuid) {
		log.info("RestMemberBioQueryAdapter : findBioByMemberUuid : Member Service 프로필 조회 시작 - memberUuid={}", memberUuid);
		try {
			MemberProfileResponse response = restClient.get()
					.uri(properties.getMember().getProfilePath(), memberUuid)
					.retrieve()
					.body(MemberProfileResponse.class);
			String bio = response == null ? null : response.bio();
			log.info("RestMemberBioQueryAdapter : findBioByMemberUuid : Member Service 프로필 조회 완료 - memberUuid={}", memberUuid);
			return Optional.ofNullable(bio);
		} catch (RestClientException exception) {
			log.warn(
					"RestMemberBioQueryAdapter : findBioByMemberUuid : Member Service 프로필 조회 실패 - memberUuid={}",
					memberUuid
			);
			return Optional.empty();
		}
	}

	public record MemberProfileResponse(
			String memberUuid,
			String nickname,
			String profileImageUrl,
			String bio
	) {
	}
}
