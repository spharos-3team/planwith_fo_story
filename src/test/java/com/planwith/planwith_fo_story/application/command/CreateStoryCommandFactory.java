package com.planwith.planwith_fo_story.application.command;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.planwith.planwith_fo_story.domain.model.VisibilityScope;

public final class CreateStoryCommandFactory {

	private CreateStoryCommandFactory() {
	}

	public static CreateStoryCommand basic(
			UUID memberUuid,
			String title,
			String content,
			String coverImageUrl,
			VisibilityScope visibilityScope
	) {
		return new CreateStoryCommand(
				memberUuid,
				null,
				false,
				title,
				content,
				coverImageUrl,
				LocalDate.of(2026, 8, 1),
				LocalDate.of(2026, 8, 5),
				true,
				visibilityScope,
				false,
				List.of(new CreateStoryCommand.Country(
						"Korea",
						0,
						List.of(new CreateStoryCommand.City("Seoul", 0, List.of()))
				)),
				List.of(),
				List.of()
		);
	}
}
