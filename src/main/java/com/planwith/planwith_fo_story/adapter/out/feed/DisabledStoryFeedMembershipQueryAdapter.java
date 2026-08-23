package com.planwith.planwith_fo_story.adapter.out.feed;

import java.util.Set;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_story.application.port.out.StoryFeedMembershipQueryPort;

@Component
@ConditionalOnProperty(prefix = "story.feed", name = "membership-query-enabled", havingValue = "false", matchIfMissing = true)
public class DisabledStoryFeedMembershipQueryAdapter implements StoryFeedMembershipQueryPort {

	@Override
	public Set<UUID> findJoinedCreatorUuids(UUID viewerUuid) {
		return Set.of();
	}
}
