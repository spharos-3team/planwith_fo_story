package com.planwith.planwith_fo_story.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.planwith.planwith_fo_story.adapter.out.persistence.outbox.SpringDataStoryOutboxRepository;
import com.planwith.planwith_fo_story.adapter.out.persistence.story.SpringDataStoryRepository;
import com.planwith.planwith_fo_story.application.command.CreateStoryCommand;
import com.planwith.planwith_fo_story.application.command.UpdateStoryCommand;
import com.planwith.planwith_fo_story.application.port.in.StoryCommandUseCase;
import com.planwith.planwith_fo_story.application.port.out.ScheduleOwnershipPort;
import com.planwith.planwith_fo_story.application.query.StoryDetailView;
import com.planwith.planwith_fo_story.domain.exception.ScheduleNotOwnedException;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;

@SpringBootTest
@ActiveProfiles("test")
class StoryScheduleOwnershipIntegrationTest {

	@Autowired
	private StoryCommandUseCase storyCommandUseCase;

	@Autowired
	private SpringDataStoryRepository storyRepository;

	@Autowired
	private SpringDataStoryOutboxRepository outboxRepository;

	@MockitoBean
	private ScheduleOwnershipPort scheduleOwnershipPort;

	@BeforeEach
	void setUp() {
		outboxRepository.deleteAll();
		storyRepository.deleteAll();
	}

	@Test
	void createRejectsScheduleNotOwnedByAuthor() {
		UUID memberUuid = UUID.randomUUID();
		UUID scheduleUuid = UUID.randomUUID();
		when(scheduleOwnershipPort.isOwnedBy(scheduleUuid, memberUuid)).thenReturn(false);

		assertThatThrownBy(() -> storyCommandUseCase.create(createCommand(memberUuid, scheduleUuid, false)))
				.isInstanceOf(ScheduleNotOwnedException.class)
				.hasMessageContaining("본인 일정");

		assertThat(storyRepository.count()).isZero();
		assertThat(outboxRepository.count()).isZero();
	}

	@Test
	void createSkipsOwnershipCheckWhenScheduleIsAbsent() {
		UUID memberUuid = UUID.randomUUID();

		storyCommandUseCase.create(createCommand(memberUuid, null, false));

		verify(scheduleOwnershipPort, never()).isOwnedBy(any(), any());
		assertThat(storyRepository.count()).isEqualTo(1);
	}

	@Test
	void updateRejectsScheduleNotOwnedByAuthor() {
		UUID memberUuid = UUID.randomUUID();
		UUID ownedScheduleUuid = UUID.randomUUID();
		UUID otherScheduleUuid = UUID.randomUUID();
		when(scheduleOwnershipPort.isOwnedBy(ownedScheduleUuid, memberUuid)).thenReturn(true);

		StoryDetailView created = storyCommandUseCase.create(createCommand(memberUuid, ownedScheduleUuid, false));

		when(scheduleOwnershipPort.isOwnedBy(otherScheduleUuid, memberUuid)).thenReturn(false);

		assertThatThrownBy(() -> storyCommandUseCase.update(new UpdateStoryCommand(
				memberUuid,
				UUID.fromString(created.storyUuid()),
				otherScheduleUuid,
				false,
				created.title(),
				created.content(),
				created.coverImageUrl(),
				created.startDate(),
				created.endDate()
		))).isInstanceOf(ScheduleNotOwnedException.class);

		assertThat(storyRepository.count()).isEqualTo(1);
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
