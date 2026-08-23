package com.planwith.planwith_fo_story.adapter.out.ai;

import java.util.List;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.planwith.planwith_fo_story.application.port.out.StoryAiModerationPort;
import com.planwith.planwith_fo_story.application.port.out.StoryAiModerationResult;
import com.planwith.planwith_fo_story.config.StoryOpenAiProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "story.openai", name = "enabled", havingValue = "true")
public class OpenAiStoryModerationAdapter implements StoryAiModerationPort {

	private final RestClient restClient;
	private final StoryOpenAiProperties properties;

	public OpenAiStoryModerationAdapter(RestClient.Builder restClientBuilder, StoryOpenAiProperties properties) {
		this.properties = properties;
		this.restClient = restClientBuilder.baseUrl(properties.getBaseUrl()).build();
	}

	@Override
	public Optional<StoryAiModerationResult> moderate(String title, String content) {
		if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
			log.warn("OpenAiStoryModerationAdapter : moderate : OpenAI API 키가 없어 검증을 건너뜁니다");
			return Optional.empty();
		}
		log.info("OpenAiStoryModerationAdapter : moderate : OpenAI 스토리 검증 시작 - model={}", properties.getModel());
		try {
			ModerationResponse response = restClient.post()
					.uri(properties.getModerationPath())
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
					.contentType(MediaType.APPLICATION_JSON)
					.body(new ModerationRequest(properties.getModel(), toInput(title, content)))
					.retrieve()
					.body(ModerationResponse.class);
			Optional<StoryAiModerationResult> result = toResult(response, properties.getModel());
			result.ifPresent(mapped -> log.info(
					"OpenAiStoryModerationAdapter : moderate : OpenAI 스토리 검증 완료 - verified={}, inputTokens={}, outputTokens={}, totalTokens={}",
					mapped.verified(),
					mapped.inputTokens(),
					mapped.outputTokens(),
					mapped.totalTokens()
			));
			return result;
		} catch (RestClientException exception) {
			log.warn("OpenAiStoryModerationAdapter : moderate : OpenAI 스토리 검증 실패");
			return Optional.empty();
		}
	}

	static Optional<StoryAiModerationResult> toResult(ModerationResponse response, String fallbackModel) {
		if (response == null || response.results() == null || response.results().isEmpty()) {
			return Optional.empty();
		}
		boolean flagged = response.results().stream().anyMatch(ModerationResult::flagged);
		Usage usage = response.usage() == null ? Usage.empty() : response.usage();
		String model = response.model() == null || response.model().isBlank() ? fallbackModel : response.model();
		return Optional.of(new StoryAiModerationResult(
				!flagged,
				usage.resolveInputTokens(),
				usage.resolveOutputTokens(),
				usage.resolveTotalTokens(),
				model
		));
	}

	private static String toInput(String title, String content) {
		String resolvedTitle = title == null ? "" : title;
		String resolvedContent = content == null ? "" : content;
		return resolvedTitle + "\n" + resolvedContent;
	}

	public record ModerationRequest(String model, String input) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record ModerationResponse(
			String id,
			String model,
			List<ModerationResult> results,
			Usage usage
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record ModerationResult(boolean flagged) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Usage(
			@JsonProperty("prompt_tokens") Integer promptTokens,
			@JsonProperty("completion_tokens") Integer completionTokens,
			@JsonProperty("total_tokens") Integer totalTokens,
			@JsonProperty("input_tokens") Integer inputTokens,
			@JsonProperty("output_tokens") Integer outputTokens
	) {
		static Usage empty() {
			return new Usage(0, 0, 0, 0, 0);
		}

		int resolveInputTokens() {
			return firstNonNull(promptTokens, inputTokens);
		}

		int resolveOutputTokens() {
			return firstNonNull(completionTokens, outputTokens);
		}

		int resolveTotalTokens() {
			int resolved = totalTokens == null ? 0 : totalTokens;
			if (resolved > 0) {
				return resolved;
			}
			return resolveInputTokens() + resolveOutputTokens();
		}

		private static int firstNonNull(Integer primary, Integer secondary) {
			if (primary != null) {
				return primary;
			}
			return secondary == null ? 0 : secondary;
		}
	}
}
