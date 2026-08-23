package com.planwith.planwith_fo_story.composition.adapter.out.like;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_story.composition.application.port.out.StoryLikeStatusQueryPort;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(
		prefix = "story.detail-screen.like",
		name = "query-enabled",
		havingValue = "false",
		matchIfMissing = true
)
public class DisabledStoryLikeStatusQueryAdapter implements StoryLikeStatusQueryPort {

	@Override
	public boolean isLikedByViewer(UUID storyUuid, UUID viewerUuid) {
		log.debug(
				"DisabledStoryLikeStatusQueryAdapter : isLikedByViewer : Like Service 미연동 상태 - storyUuid={}",
				storyUuid
		);
		return false;
	}
}
