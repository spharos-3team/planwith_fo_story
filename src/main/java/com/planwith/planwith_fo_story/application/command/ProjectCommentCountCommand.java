package com.planwith.planwith_fo_story.application.command;

import java.util.UUID;

public record ProjectCommentCountCommand(
		String targetType,
		UUID targetUuid,
		long sourceVersion
) {
	public boolean isStoryTarget() {
		return "STORY".equalsIgnoreCase(targetType);
	}
}
