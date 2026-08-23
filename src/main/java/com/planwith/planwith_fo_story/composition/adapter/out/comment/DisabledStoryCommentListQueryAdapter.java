package com.planwith.planwith_fo_story.composition.adapter.out.comment;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_story.composition.application.port.out.StoryCommentListQueryPort;
import com.planwith.planwith_fo_story.composition.application.query.StoryCommentItemView;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(
		prefix = "story.detail-screen.comment",
		name = "query-enabled",
		havingValue = "false",
		matchIfMissing = true
)
public class DisabledStoryCommentListQueryAdapter implements StoryCommentListQueryPort {

	@Override
	public List<StoryCommentItemView> findByStoryUuid(UUID storyUuid) {
		log.debug(
				"DisabledStoryCommentListQueryAdapter : findByStoryUuid : Comment Service 미연동 상태 - storyUuid={}",
				storyUuid
		);
		return Collections.emptyList();
	}
}
