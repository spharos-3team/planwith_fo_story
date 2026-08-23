package com.planwith.planwith_fo_story.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.planwith.planwith_fo_story.domain.model.Story;
import com.planwith.planwith_fo_story.application.query.StorySortType;

public interface StoryQueryPort {

	Optional<Story> findByStoryUuid(UUID storyUuid);

	Optional<Story> findActiveByStoryUuid(UUID storyUuid);

	List<Story> findActiveByMemberUuid(UUID memberUuid, int offset, int size);

	List<Story> findRecentActive(int offset, int size);

	List<Story> findPopularActive(int size);

	List<Story> findActive(Set<UUID> authorUuids, StorySortType sort, int offset, int size);
}
