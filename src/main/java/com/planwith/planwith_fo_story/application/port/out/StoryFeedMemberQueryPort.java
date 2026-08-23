package com.planwith.planwith_fo_story.application.port.out;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface StoryFeedMemberQueryPort {

	Optional<Set<UUID>> findEligibleFollowingAuthors(UUID viewerUuid);

	Optional<Set<UUID>> filterEligibleAuthors(Set<UUID> candidateAuthorUuids);
}
