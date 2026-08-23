package com.planwith.planwith_fo_story.adapter.out.ai;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_story.application.port.out.StoryAiVerificationRequestPort;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AfterCommitStoryAiVerificationAdapter implements StoryAiVerificationRequestPort {

	@Override
	public void requestVerification(UUID storyUuid, UUID memberUuid) {
		log.info(
				"AfterCommitStoryAiVerificationAdapter : requestVerification : AI 검증 요청 접수 - storyUuid={}",
				storyUuid
		);
	}
}
