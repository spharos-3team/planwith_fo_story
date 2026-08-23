package com.planwith.planwith_fo_story.adapter.out.ai;

import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_story.application.port.out.StoryAiModerationPort;
import com.planwith.planwith_fo_story.application.port.out.StoryAiModerationResult;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "story.openai", name = "enabled", havingValue = "false", matchIfMissing = true)
public class DisabledStoryAiModerationAdapter implements StoryAiModerationPort {

	@Override
	public Optional<StoryAiModerationResult> moderate(String title, String content) {
		log.warn("DisabledStoryAiModerationAdapter : moderate : OpenAI 미연동 상태에서 AI 검증을 건너뜁니다");
		return Optional.empty();
	}
}
