package com.planwith.planwith_fo_story.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_story.domain.model.Story;
import com.planwith.planwith_fo_story.domain.model.StoryVisitCity;
import com.planwith.planwith_fo_story.domain.model.StoryVisitCountry;
import com.planwith.planwith_fo_story.domain.model.StoryVisibilityMember;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;
import com.planwith.planwith_fo_story.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_story.domain.model.vo.StoryUuid;

class StoryAccessPolicyTest {

	private final StoryAccessPolicy policy = new StoryAccessPolicy();
	private final MemberUuid author = MemberUuid.of(UUID.randomUUID());
	private final MemberUuid viewer = MemberUuid.of(UUID.randomUUID());
	private final LocalDateTime now = LocalDateTime.of(2026, 8, 23, 21, 0);

	@Test
	void authorCanReadOwnStoryForEveryScope() {
		assertThat(policy.canRead(story(VisibilityScope.ALL), author, false)).isTrue();
		assertThat(policy.canRead(story(VisibilityScope.MEMBER), author, false)).isTrue();
		assertThat(policy.canRead(story(VisibilityScope.MEMBERSHIP), author, false)).isTrue();
		assertThat(policy.canRead(privateStory(viewer), author, false)).isTrue();
	}

	@Test
	void allScopeAllowsGuestAndMember() {
		Story story = story(VisibilityScope.ALL);

		assertThat(policy.canRead(story, null, false)).isTrue();
		assertThat(policy.canRead(story, viewer, false)).isTrue();
	}

	@Test
	void memberScopeRequiresLogin() {
		Story story = story(VisibilityScope.MEMBER);

		assertThat(policy.canRead(story, null, false)).isFalse();
		assertThat(policy.canRead(story, viewer, false)).isTrue();
	}

	@Test
	void membershipScopeRequiresAuthorSubscriber() {
		Story story = story(VisibilityScope.MEMBERSHIP);

		assertThat(policy.canRead(story, viewer, false)).isFalse();
		assertThat(policy.canRead(story, null, true)).isFalse();
		assertThat(policy.canRead(story, viewer, true)).isTrue();
	}

	@Test
	void privateScopeAllowsOnlyAuthorAndDesignatedMembers() {
		Story story = privateStory(viewer);
		MemberUuid other = MemberUuid.of(UUID.randomUUID());

		assertThat(policy.canRead(story, viewer, false)).isTrue();
		assertThat(policy.canRead(story, other, false)).isFalse();
		assertThat(policy.canRead(story, null, false)).isFalse();
	}

	@Test
	void deletedStoryIsNeverReadable() {
		Story deleted = story(VisibilityScope.ALL).delete(author, now);

		assertThat(policy.canRead(deleted, author, true)).isFalse();
		assertThat(policy.canRead(deleted, viewer, true)).isFalse();
	}

	private Story story(VisibilityScope visibilityScope) {
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
				now
		);
	}

	private Story privateStory(MemberUuid designatedMember) {
		return story(VisibilityScope.PRIVATE).replaceChildren(
				List.of(StoryVisitCountry.create("Korea", 0, List.of(StoryVisitCity.create("Seoul", 0)))),
				List.of(),
				List.of(StoryVisibilityMember.create(designatedMember, now)),
				now
		);
	}
}
