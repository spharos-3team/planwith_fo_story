package com.planwith.planwith_fo_story.application.command;

import java.util.UUID;

public record ChangeStoryCommentEnabledCommand(
		UUID actorUuid,
		UUID storyUuid,
		boolean commentEnabled
) {
}
