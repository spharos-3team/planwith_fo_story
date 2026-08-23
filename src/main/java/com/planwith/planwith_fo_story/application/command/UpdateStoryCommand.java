package com.planwith.planwith_fo_story.application.command;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateStoryCommand(
		UUID actorUuid,
		UUID storyUuid,
		UUID scheduleUuid,
		boolean scheduleVisible,
		String title,
		String content,
		String coverImageUrl,
		LocalDate startDate,
		LocalDate endDate
) {
}
