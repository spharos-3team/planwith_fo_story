package com.planwith.planwith_fo_story.application.port.out;

public record StoryAiModerationResult(
		boolean verified,
		int inputTokens,
		int outputTokens,
		int totalTokens,
		String model
) {
}
