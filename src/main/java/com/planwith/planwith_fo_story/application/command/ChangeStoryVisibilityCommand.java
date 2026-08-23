package com.planwith.planwith_fo_story.application.command;

import java.util.UUID;

import com.planwith.planwith_fo_story.domain.model.VisibilityScope;

public record ChangeStoryVisibilityCommand(
		UUID actorUuid,
		UUID storyUuid,
		VisibilityScope visibilityScope
) {
}
