package com.planwith.planwith_fo_story.application.command;

import java.time.LocalDate;
import java.util.UUID;

import com.planwith.planwith_fo_story.domain.model.VisibilityScope;

public record CreateStoryCommand(
		UUID memberUuid,
		UUID scheduleUuid,
		boolean scheduleVisible,
		String title,
		String content,
		String coverImageUrl,
		LocalDate startDate,
		LocalDate endDate,
		boolean commentEnabled,
		VisibilityScope visibilityScope
) {
}
