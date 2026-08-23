package com.planwith.planwith_fo_story.adapter.in.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Comment Service counter event contract. targetType is STORY and targetUuid is the story UUID.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CommentChangedEventPayload(
		String eventUuid,
		String targetType,
		String targetUuid,
		String targetOwnerUuid,
		String occurredAt,
		Long sourceVersion
) {
}
