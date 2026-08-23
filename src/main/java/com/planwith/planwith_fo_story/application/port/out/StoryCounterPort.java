package com.planwith.planwith_fo_story.application.port.out;

import java.util.UUID;

public interface StoryCounterPort {

	boolean incrementViewCount(UUID storyUuid);

	boolean changeLikeCount(UUID storyUuid, long delta);

	boolean changeCommentCount(UUID storyUuid, long delta);
}
