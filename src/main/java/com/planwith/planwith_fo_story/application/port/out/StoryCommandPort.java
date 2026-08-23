package com.planwith.planwith_fo_story.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_story.domain.model.Story;

public interface StoryCommandPort {

	Story save(Story story);

	Optional<Story> findByStoryUuid(UUID storyUuid);
}
