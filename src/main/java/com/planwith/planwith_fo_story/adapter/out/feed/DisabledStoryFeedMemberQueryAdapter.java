package com.planwith.planwith_fo_story.adapter.out.feed;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_story.application.port.out.StoryFeedMemberQueryPort;

@Component
@ConditionalOnProperty(prefix = "story.feed", name = "member-query-enabled", havingValue = "false", matchIfMissing = true)
public class DisabledStoryFeedMemberQueryAdapter implements StoryFeedMemberQueryPort {

	@Override
	public Optional<Set<UUID>> findEligibleFollowingAuthors(UUID viewerUuid) {
		return Optional.empty();
	}

	@Override
	public Optional<Set<UUID>> filterEligibleAuthors(Set<UUID> candidateAuthorUuids) {
		return Optional.empty();
	}
}
