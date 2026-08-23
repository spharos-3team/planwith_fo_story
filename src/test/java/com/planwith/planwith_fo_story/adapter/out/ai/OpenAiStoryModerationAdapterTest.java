package com.planwith.planwith_fo_story.adapter.out.ai;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_story.application.port.out.StoryAiModerationResult;

class OpenAiStoryModerationAdapterTest {

	@Test
	void mapsUnflaggedResponseToVerifiedUsage() {
		OpenAiStoryModerationAdapter.ModerationResponse response = new OpenAiStoryModerationAdapter.ModerationResponse(
				"modr-1",
				"omni-moderation-latest",
				List.of(new OpenAiStoryModerationAdapter.ModerationResult(false)),
				new OpenAiStoryModerationAdapter.Usage(10, 0, 10, null, null)
		);

		Optional<StoryAiModerationResult> result = OpenAiStoryModerationAdapter.toResult(
				response,
				"omni-moderation-latest"
		);

		assertThat(result).isPresent();
		assertThat(result.get().verified()).isTrue();
		assertThat(result.get().inputTokens()).isEqualTo(10);
		assertThat(result.get().outputTokens()).isZero();
		assertThat(result.get().totalTokens()).isEqualTo(10);
		assertThat(result.get().model()).isEqualTo("omni-moderation-latest");
	}

	@Test
	void mapsFlaggedResponseToUnverified() {
		OpenAiStoryModerationAdapter.ModerationResponse response = new OpenAiStoryModerationAdapter.ModerationResponse(
				"modr-2",
				"omni-moderation-latest",
				List.of(new OpenAiStoryModerationAdapter.ModerationResult(true)),
				new OpenAiStoryModerationAdapter.Usage(null, null, null, 8, 1)
		);

		StoryAiModerationResult result = OpenAiStoryModerationAdapter.toResult(response, "fallback").orElseThrow();

		assertThat(result.verified()).isFalse();
		assertThat(result.inputTokens()).isEqualTo(8);
		assertThat(result.outputTokens()).isEqualTo(1);
		assertThat(result.totalTokens()).isEqualTo(9);
	}

	@Test
	void mapsMissingResponseToEmpty() {
		assertThat(OpenAiStoryModerationAdapter.toResult(null, "omni-moderation-latest")).isEmpty();
		assertThat(OpenAiStoryModerationAdapter.toResult(
				new OpenAiStoryModerationAdapter.ModerationResponse("modr-3", "model", List.of(), null),
				"model"
		)).isEmpty();
	}
}
