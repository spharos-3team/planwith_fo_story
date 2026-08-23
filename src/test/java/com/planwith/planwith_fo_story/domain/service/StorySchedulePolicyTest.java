package com.planwith.planwith_fo_story.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_story.domain.model.vo.MemberUuid;

class StorySchedulePolicyTest {

	private final StorySchedulePolicy policy = new StorySchedulePolicy();
	private final MemberUuid author = MemberUuid.of(UUID.randomUUID());
	private final MemberUuid viewer = MemberUuid.of(UUID.randomUUID());
	private final UUID scheduleUuid = UUID.randomUUID();

	@Test
	void authorCanAlwaysSeeAttachedSchedule() {
		assertThat(policy.canExposeScheduleReference(author, author, scheduleUuid, false)).isTrue();
		assertThat(policy.canExposeScheduleReference(author, author, scheduleUuid, true)).isTrue();
	}

	@Test
	void otherViewerSeesScheduleOnlyWhenVisible() {
		assertThat(policy.canExposeScheduleReference(author, viewer, scheduleUuid, false)).isFalse();
		assertThat(policy.canExposeScheduleReference(author, viewer, scheduleUuid, true)).isTrue();
	}

	@Test
	void anonymousViewerSeesScheduleOnlyWhenVisible() {
		assertThat(policy.canExposeScheduleReference(author, null, scheduleUuid, false)).isFalse();
		assertThat(policy.canExposeScheduleReference(author, null, scheduleUuid, true)).isTrue();
	}

	@Test
	void missingScheduleIsNeverExposed() {
		assertThat(policy.canExposeScheduleReference(author, author, null, true)).isFalse();
		assertThat(policy.canExposeScheduleReference(author, viewer, null, true)).isFalse();
	}
}
