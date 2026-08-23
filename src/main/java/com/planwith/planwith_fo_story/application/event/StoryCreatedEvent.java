package com.planwith.planwith_fo_story.application.event;

import java.time.Instant;

public record StoryCreatedEvent(
		String eventUuid,
		String memberUuid,
		String storyUuid,
		String occurredAt,
		Long sourceVersion
) {
	public static final String EVENT_TYPE = "StoryCreated";

	public static StoryCreatedEvent of(String eventUuid, String memberUuid, String storyUuid, Instant occurredAt) {
		return new StoryCreatedEvent(
				eventUuid,
				memberUuid,
				storyUuid,
				occurredAt.toString(),
				occurredAt.toEpochMilli()
		);
	}
}
