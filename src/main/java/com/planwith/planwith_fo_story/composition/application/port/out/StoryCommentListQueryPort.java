package com.planwith.planwith_fo_story.composition.application.port.out;

import java.util.List;
import java.util.UUID;

import com.planwith.planwith_fo_story.composition.application.query.StoryCommentItemView;

public interface StoryCommentListQueryPort {

	List<StoryCommentItemView> findByStoryUuid(UUID storyUuid);
}
