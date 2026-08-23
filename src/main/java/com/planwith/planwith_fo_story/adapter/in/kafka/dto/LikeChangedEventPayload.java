package com.planwith.planwith_fo_story.adapter.in.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LikeChangedEventPayload(
		String eventUuid,
		String targetType,
		String targetUuid,
		String targetOwnerUuid,
		String occurredAt,
		Long sourceVersion
) {
}
