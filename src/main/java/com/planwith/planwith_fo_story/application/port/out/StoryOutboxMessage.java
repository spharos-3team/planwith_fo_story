package com.planwith.planwith_fo_story.application.port.out;

public record StoryOutboxMessage(
		String eventUuid,
		String aggregateType,
		String aggregateUuid,
		String eventType,
		String payload
) {
	public static final String AGGREGATE_TYPE = "Story";
}
