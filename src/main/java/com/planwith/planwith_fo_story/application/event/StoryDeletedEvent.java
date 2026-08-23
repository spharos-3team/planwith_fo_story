package com.planwith.planwith_fo_story.application.event;

import java.time.Instant;

public record StoryDeletedEvent(
		String eventUuid,
		String memberUuid,
		String storyUuid,
		String occurredAt,
		Long sourceVersion
) {
	public static final String EVENT_TYPE = "StoryDeleted";

	public static StoryDeletedEvent of(String eventUuid, String memberUuid, String storyUuid, Instant occurredAt) {
		return new StoryDeletedEvent(
				eventUuid,
				memberUuid,
				storyUuid,
				occurredAt.toString(),
				occurredAt.toEpochMilli()
		);
	}
}
