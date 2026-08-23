package com.planwith.planwith_fo_story.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.planwith.planwith_fo_story.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_story.domain.model.vo.StoryUuid;

final class StoryTestFactory {

	private StoryTestFactory() {
	}

	static Story create(MemberUuid author, VisibilityScope visibilityScope) {
		return Story.create(
				StoryUuid.generate(),
				author,
				null,
				false,
				"제목",
				"본문",
				"https://img.example/cover.png",
				LocalDate.of(2026, 8, 1),
				LocalDate.of(2026, 8, 5),
				true,
				visibilityScope,
				LocalDateTime.of(2026, 8, 23, 11, 0)
		);
	}
}
