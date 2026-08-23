package com.planwith.planwith_fo_story.application.port.in;

import com.planwith.planwith_fo_story.application.query.GetStoryDetailQuery;
import com.planwith.planwith_fo_story.application.query.GetStoryFeedQuery;
import com.planwith.planwith_fo_story.application.query.GetStoryListQuery;
import com.planwith.planwith_fo_story.application.query.SearchStoryQuery;
import com.planwith.planwith_fo_story.application.query.StoryDetailView;
import com.planwith.planwith_fo_story.application.query.StoryFeedView;
import com.planwith.planwith_fo_story.application.query.StoryListView;

public interface StoryQueryUseCase {

	StoryDetailView getDetail(GetStoryDetailQuery query);

	StoryListView getList(GetStoryListQuery query);

	StoryFeedView getFeed(GetStoryFeedQuery query);

	StoryListView search(SearchStoryQuery query);
}
