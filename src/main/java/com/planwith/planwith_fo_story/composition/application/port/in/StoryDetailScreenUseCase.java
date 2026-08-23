package com.planwith.planwith_fo_story.composition.application.port.in;

import com.planwith.planwith_fo_story.composition.application.query.GetStoryDetailScreenQuery;
import com.planwith.planwith_fo_story.composition.application.query.StoryDetailScreenView;

public interface StoryDetailScreenUseCase {

	StoryDetailScreenView compose(GetStoryDetailScreenQuery query);
}
