package com.planwith.planwith_fo_story.adapter.in.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MemberProfileChangedEventPayload(
		String eventUuid,
		String memberUuid,
		String nickname,
		String profileImage,
		String memberStatus,
		String occurredAt,
		Long sourceVersion
) {
}
