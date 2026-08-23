package com.planwith.planwith_fo_story.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
import com.planwith.planwith_fo_story.application.event.StoryAiUsageRecordedEvent;
import com.planwith.planwith_fo_story.application.event.StoryCreatedEvent;
import com.planwith.planwith_fo_story.application.port.in.StoryCommandUseCase;
import com.planwith.planwith_fo_story.application.port.in.StoryQueryUseCase;
import com.planwith.planwith_fo_story.application.port.out.StoryAiModerationPort;
import com.planwith.planwith_fo_story.application.port.out.StoryAiModerationResult;
import com.planwith.planwith_fo_story.application.query.GetStoryDetailQuery;
import com.planwith.planwith_fo_story.application.query.StoryDetailView;
import com.planwith.planwith_fo_story.domain.model.AiModerationStatus;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;

@SpringBootTest
@ActiveProfiles("test")
class StoryAiVerificationIntegrationTest {

	@Autowired
	private StoryCommandUseCase storyCommandUseCase;

	@Autowired
	private StoryQueryUseCase storyQueryUseCase;

	@Autowired
	private SpringDataStoryRepository storyRepository;

	@Autowired
	private SpringDataStoryOutboxRepository outboxRepository;

	@MockitoBean
	private StoryAiModerationPort storyAiModerationPort;

	@BeforeEach
	void setUp() {
		outboxRepository.deleteAll();
		storyRepository.deleteAll();
	}

	@Test
	void successfulModerationMarksStoryVerifiedAndRecordsUsageOutbox() {
		when(storyAiModerationPort.moderate(any(), any())).thenReturn(Optional.of(
				new StoryAiModerationResult(true, 12, 3, 15, "omni-moderation-latest")
		));
		UUID memberUuid = UUID.randomUUID();

		StoryDetailView created = storyCommandUseCase.create(createCommand(memberUuid, true));

		assertThat(created.aiModerationStatus()).isEqualTo(AiModerationStatus.UNVERIFIED);
		StoryDetailView detail = storyQueryUseCase.getDetail(new GetStoryDetailQuery(
				UUID.fromString(created.storyUuid()),
				memberUuid
		));
		assertThat(detail.aiModerationStatus()).isEqualTo(AiModerationStatus.VERIFIED);
		assertThat(outboxRepository.findAll())
				.extracting(outbox -> outbox.eventType())
				.containsExactlyInAnyOrder(StoryCreatedEvent.EVENT_TYPE, StoryAiUsageRecordedEvent.EVENT_TYPE);
		assertThat(outboxRepository.findAll()).anySatisfy(outbox -> {
			if (StoryAiUsageRecordedEvent.EVENT_TYPE.equals(outbox.eventType())) {
				assertThat(outbox.eventUuid().toString()).isEqualTo(extractRequestId(outbox.payload()));
				assertThat(outbox.payload()).contains("\"inputTokens\":12");
				assertThat(outbox.payload()).contains("\"outputTokens\":3");
				assertThat(outbox.payload()).contains("\"totalTokens\":15");
				assertThat(outbox.payload()).contains("\"model\":\"omni-moderation-latest\"");
				assertThat(outbox.payload()).doesNotContain("price");
				assertThat(outbox.payload()).doesNotContain("amount");
			}
		});
	}

	@Test
	void flaggedModerationKeepsUnverifiedAndStillRecordsUsage() {
		when(storyAiModerationPort.moderate(any(), any())).thenReturn(Optional.of(
				new StoryAiModerationResult(false, 9, 1, 10, "omni-moderation-latest")
		));
		UUID memberUuid = UUID.randomUUID();
		StoryDetailView created = storyCommandUseCase.create(createCommand(memberUuid, true));

		StoryDetailView detail = storyQueryUseCase.getDetail(new GetStoryDetailQuery(
				UUID.fromString(created.storyUuid()),
				memberUuid
		));
		assertThat(detail.aiModerationStatus()).isEqualTo(AiModerationStatus.UNVERIFIED);
		assertThat(outboxRepository.findAll())
				.extracting(outbox -> outbox.eventType())
				.contains(StoryAiUsageRecordedEvent.EVENT_TYPE);
	}

	@Test
	void failedModerationKeepsUnverifiedWithoutUsageOutbox() {
		when(storyAiModerationPort.moderate(any(), any())).thenReturn(Optional.empty());
		UUID memberUuid = UUID.randomUUID();
		StoryDetailView created = storyCommandUseCase.create(createCommand(memberUuid, true));

		StoryDetailView detail = storyQueryUseCase.getDetail(new GetStoryDetailQuery(
				UUID.fromString(created.storyUuid()),
				memberUuid
		));
		assertThat(detail.aiModerationStatus()).isEqualTo(AiModerationStatus.UNVERIFIED);
		assertThat(outboxRepository.findAll())
				.extracting(outbox -> outbox.eventType())
				.containsExactly(StoryCreatedEvent.EVENT_TYPE);
	}

	@Test
	void skippedVerificationDoesNotCallModeration() {
		UUID memberUuid = UUID.randomUUID();
		storyCommandUseCase.create(createCommand(memberUuid, false));

		verify(storyAiModerationPort, never()).moderate(any(), any());
		assertThat(outboxRepository.findAll())
				.extracting(outbox -> outbox.eventType())
				.containsExactly(StoryCreatedEvent.EVENT_TYPE);
	}

	private static CreateStoryCommand createCommand(UUID memberUuid, boolean aiVerificationRequested) {
		return new CreateStoryCommand(
				memberUuid,
				null,
				false,
				"AI 검증 스토리",
				"본문입니다.",
				"https://img.example/cover.png",
				LocalDate.of(2026, 8, 1),
				LocalDate.of(2026, 8, 5),
				true,
				VisibilityScope.ALL,
				aiVerificationRequested,
				List.of(new CreateStoryCommand.Country(
						"Korea",
						0,
						List.of(new CreateStoryCommand.City("Seoul", 0, List.of()))
				)),
				List.of(),
				List.of()
		);
	}

	private static String extractRequestId(String payload) {
		int start = payload.indexOf("\"requestId\":\"") + "\"requestId\":\"".length();
		int end = payload.indexOf('"', start);
		return payload.substring(start, end);
	}
}
