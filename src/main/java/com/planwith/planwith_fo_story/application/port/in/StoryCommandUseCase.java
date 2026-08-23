package com.planwith.planwith_fo_story.application.port.in;

import com.planwith.planwith_fo_story.application.command.ChangeStoryCommentEnabledCommand;
import com.planwith.planwith_fo_story.application.command.ChangeStoryVisibilityCommand;
import com.planwith.planwith_fo_story.application.command.CreateStoryCommand;
import com.planwith.planwith_fo_story.application.command.DeleteStoryCommand;
import com.planwith.planwith_fo_story.application.command.IncreaseStoryViewCountCommand;
import com.planwith.planwith_fo_story.application.command.UpdateStoryCommand;
import com.planwith.planwith_fo_story.application.query.StoryDetailView;

public interface StoryCommandUseCase {

	StoryDetailView create(CreateStoryCommand command);

	StoryDetailView update(UpdateStoryCommand command);

	void delete(DeleteStoryCommand command);

	StoryDetailView changeVisibility(ChangeStoryVisibilityCommand command);

	StoryDetailView changeCommentEnabled(ChangeStoryCommentEnabledCommand command);

	void increaseViewCount(IncreaseStoryViewCountCommand command);
}
