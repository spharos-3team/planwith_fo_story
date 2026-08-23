package com.planwith.planwith_fo_story.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_story.domain.model.Story;

public interface StoryQueryPort {

	Optional<Story> findActiveByStoryUuid(UUID storyUuid);

	List<Story> findActiveByMemberUuid(UUID memberUuid, int offset, int size);

	List<Story> findRecentActive(int offset, int size);

	List<Story> findPopularActive(int size);
}
