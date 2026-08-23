package com.planwith.planwith_fo_story.application.event;

import java.time.Instant;

public record StoryAiUsageRecordedEvent(
		String requestId,
		String memberUuid,
		String storyUuid,
		int inputTokens,
		int outputTokens,
		int totalTokens,
		String model,
		String occurredAt
) {
	public static final String EVENT_TYPE = "StoryAiUsageRecorded";

	public static StoryAiUsageRecordedEvent of(
			String requestId,
			String memberUuid,
			String storyUuid,
			int inputTokens,
			int outputTokens,
			int totalTokens,
			String model,
			Instant occurredAt
	) {
		return new StoryAiUsageRecordedEvent(
				requestId,
				memberUuid,
				storyUuid,
				inputTokens,
				outputTokens,
				totalTokens,
				model,
				occurredAt.toString()
		);
	}
}
