package com.planwith.planwith_fo_story.composition.adapter.out.follow;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_story.composition.application.port.out.FollowSummaryQueryPort;
import com.planwith.planwith_fo_story.composition.application.query.StoryDetailScreenView.FollowScreenView;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(
		prefix = "story.detail-screen.follow",
		name = "query-enabled",
		havingValue = "false",
		matchIfMissing = true
)
public class DisabledFollowSummaryQueryAdapter implements FollowSummaryQueryPort {

	@Override
	public FollowScreenView findByMemberUuid(UUID memberUuid) {
		log.debug(
				"DisabledFollowSummaryQueryAdapter : findByMemberUuid : Follow Service 미연동 상태 - memberUuid={}",
				memberUuid
		);
		return new FollowScreenView(0L, 0L);
	}
}
