package com.planwith.planwith_fo_story.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.planwith.planwith_fo_story.adapter.out.persistence.outbox.SpringDataStoryOutboxRepository;
import com.planwith.planwith_fo_story.adapter.out.persistence.story.SpringDataStoryRepository;
import com.planwith.planwith_fo_story.application.command.CreateStoryCommand;
import com.planwith.planwith_fo_story.application.command.UpdateStoryCommand;
import com.planwith.planwith_fo_story.application.port.in.StoryCommandUseCase;
import com.planwith.planwith_fo_story.application.port.in.StoryQueryUseCase;
import com.planwith.planwith_fo_story.application.query.GetStoryDetailQuery;
import com.planwith.planwith_fo_story.application.query.StoryDetailView;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;

@SpringBootTest
@ActiveProfiles("test")
class StoryScheduleAttachIntegrationTest {

	@Autowired
	private StoryCommandUseCase storyCommandUseCase;

	@Autowired
	private StoryQueryUseCase storyQueryUseCase;

	@Autowired
	private SpringDataStoryRepository storyRepository;

	@Autowired
	private SpringDataStoryOutboxRepository outboxRepository;

	@BeforeEach
	void setUp() {
		outboxRepository.deleteAll();
		storyRepository.deleteAll();
	}

	@Test
	void hiddenScheduleUuidIsVisibleOnlyToAuthor() {
		UUID authorUuid = UUID.randomUUID();
		UUID scheduleUuid = UUID.randomUUID();
		StoryDetailView created = storyCommandUseCase.create(createCommand(
				authorUuid,
				scheduleUuid,
				false
		));

		assertThat(created.scheduleUuid()).isEqualTo(scheduleUuid.toString());
		assertThat(created.scheduleVisible()).isFalse();

		StoryDetailView authorView = storyQueryUseCase.getDetail(new GetStoryDetailQuery(
				UUID.fromString(created.storyUuid()),
				authorUuid
		));
		assertThat(authorView.scheduleUuid()).isEqualTo(scheduleUuid.toString());
		assertThat(authorView.scheduleVisible()).isFalse();

		StoryDetailView otherView = storyQueryUseCase.getDetail(new GetStoryDetailQuery(
				UUID.fromString(created.storyUuid()),
				UUID.randomUUID()
		));
		assertThat(otherView.scheduleUuid()).isNull();
		assertThat(otherView.scheduleVisible()).isFalse();
	}

	@Test
	void visibleScheduleUuidIsExposedToOtherViewers() {
		UUID authorUuid = UUID.randomUUID();
		UUID scheduleUuid = UUID.randomUUID();
		StoryDetailView created = storyCommandUseCase.create(createCommand(
				authorUuid,
				scheduleUuid,
				true
		));

		StoryDetailView otherView = storyQueryUseCase.getDetail(new GetStoryDetailQuery(
				UUID.fromString(created.storyUuid()),
				UUID.randomUUID()
		));
		assertThat(otherView.scheduleUuid()).isEqualTo(scheduleUuid.toString());
		assertThat(otherView.scheduleVisible()).isTrue();
	}

	@Test
	void updateCanDetachSchedule() {
		UUID authorUuid = UUID.randomUUID();
		StoryDetailView created = storyCommandUseCase.create(createCommand(
				authorUuid,
				UUID.randomUUID(),
				false
		));

		StoryDetailView updated = storyCommandUseCase.update(new UpdateStoryCommand(
				authorUuid,
				UUID.fromString(created.storyUuid()),
				null,
				false,
				created.title(),
				created.content(),
				created.coverImageUrl(),
				created.startDate(),
				created.endDate()
		));

		assertThat(updated.scheduleUuid()).isNull();
		assertThat(updated.scheduleVisible()).isFalse();
	}

	private static CreateStoryCommand createCommand(UUID memberUuid, UUID scheduleUuid, boolean scheduleVisible) {
		return new CreateStoryCommand(
				memberUuid,
				scheduleUuid,
				scheduleVisible,
				"일정 첨부 스토리",
				"본문입니다.",
				"https://img.example/cover.png",
				LocalDate.of(2026, 8, 1),
				LocalDate.of(2026, 8, 5),
				true,
				VisibilityScope.ALL,
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
