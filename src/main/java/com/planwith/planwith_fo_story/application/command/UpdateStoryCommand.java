package com.planwith.planwith_fo_story.application.command;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateStoryCommand(
		UUID actorUuid,
		UUID storyUuid,
		String title,
		String content,
		String coverImageUrl,
		String visitCountry,
		String visitCity,
		String visitPlace,
		LocalDate startDate,
		LocalDate endDate
) {
}
