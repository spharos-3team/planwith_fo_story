package com.planwith.planwith_fo_story.application.command;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.planwith.planwith_fo_story.domain.model.VisibilityScope;

public record UpdateStoryCommand(
		UUID actorUuid,
		UUID storyUuid,
		UUID scheduleUuid,
		boolean scheduleVisible,
		String title,
		String content,
		String coverImageUrl,
		LocalDate startDate,
		LocalDate endDate,
		boolean commentEnabled,
		VisibilityScope visibilityScope,
		boolean aiVerificationRequested,
		List<CreateStoryCommand.Country> countries,
		List<String> tags,
		List<UUID> visibilityMemberUuids
) {
	public UpdateStoryCommand {
		countries = countries == null ? List.of() : List.copyOf(countries);
		tags = tags == null ? List.of() : List.copyOf(tags);
		visibilityMemberUuids = visibilityMemberUuids == null ? List.of() : List.copyOf(visibilityMemberUuids);
	}

	public UpdateStoryCommand(
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
		this(
				actorUuid, storyUuid, scheduleUuid, scheduleVisible, title, content, coverImageUrl,
				startDate, endDate, true, VisibilityScope.ALL, false, List.of(), List.of(), List.of()
		);
	}
}
