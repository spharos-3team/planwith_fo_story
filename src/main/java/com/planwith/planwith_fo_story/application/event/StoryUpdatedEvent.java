package com.planwith.planwith_fo_story.application.event;

import java.time.Instant;

public record StoryUpdatedEvent(
		String eventUuid,
		String memberUuid,
		String storyUuid,
		String occurredAt,
		Long sourceVersion
) {
	public static final String EVENT_TYPE = "StoryUpdated";

	public static StoryUpdatedEvent of(String eventUuid, String memberUuid, String storyUuid, Instant occurredAt) {
		return new StoryUpdatedEvent(
				eventUuid,
				memberUuid,
				storyUuid,
				occurredAt.toString(),
				occurredAt.toEpochMilli()
		);
	}
}
