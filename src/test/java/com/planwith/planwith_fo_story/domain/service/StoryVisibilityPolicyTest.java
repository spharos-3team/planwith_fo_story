package com.planwith.planwith_fo_story.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_story.domain.model.Story;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;
import com.planwith.planwith_fo_story.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_story.domain.model.vo.StoryUuid;

class StoryVisibilityPolicyTest {

	private final StoryVisibilityPolicy policy = new StoryVisibilityPolicy();
	private final MemberUuid author = MemberUuid.of(UUID.randomUUID());
	private final MemberUuid viewer = MemberUuid.of(UUID.randomUUID());

	@Test
	void authorCanAlwaysViewOwnStory() {
		Story story = story(VisibilityScope.PRIVATE);

		assertThat(policy.canView(story, author, false)).isTrue();
	}

	@Test
	void membershipStoryRequiresEntitlement() {
		Story story = story(VisibilityScope.MEMBERSHIP);

		assertThat(policy.canView(story, viewer, false)).isFalse();
		assertThat(policy.canView(story, viewer, true)).isTrue();
	}

	private Story story(VisibilityScope visibilityScope) {
		return Story.create(
				StoryUuid.generate(),
				author,
				null,
				"제목",
				"본문",
				null,
				null,
				null,
				null,
				null,
				null,
				true,
				visibilityScope,
				LocalDateTime.of(2026, 8, 23, 11, 0)
		);
	}
}
