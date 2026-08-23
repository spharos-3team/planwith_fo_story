package com.planwith.planwith_fo_story.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_story.domain.model.projection.StoryLikeCountProjection;

public interface StoryLikeCountProjectionPort {

	void save(StoryLikeCountProjection projection);

	Optional<StoryLikeCountProjection> findByStoryUuid(UUID storyUuid);
}
