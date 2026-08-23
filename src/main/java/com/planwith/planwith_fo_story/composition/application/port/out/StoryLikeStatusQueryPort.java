package com.planwith.planwith_fo_story.composition.application.port.out;

import java.util.UUID;

public interface StoryLikeStatusQueryPort {

	boolean isLikedByViewer(UUID storyUuid, UUID viewerUuid);
}
