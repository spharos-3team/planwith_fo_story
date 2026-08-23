package com.planwith.planwith_fo_story.adapter.in.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MembershipChangedEventPayload(
		String eventUuid,
		String memberUuid,
		String creatorUuid,
		String membershipUuid,
		String occurredAt,
		Long sourceVersion
) {
}
