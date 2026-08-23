package com.planwith.planwith_fo_story.application.command;

import java.util.UUID;

public record DeleteStoryCommand(
		UUID actorUuid,
		UUID storyUuid
) {
}
