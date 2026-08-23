package com.planwith.planwith_fo_story.composition.application.port.out;

import java.util.UUID;

import com.planwith.planwith_fo_story.composition.application.query.StoryDetailScreenView.FollowScreenView;

public interface FollowSummaryQueryPort {

	FollowScreenView findByMemberUuid(UUID memberUuid);
}
