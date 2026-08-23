package com.planwith.planwith_fo_story.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_story.adapter.out.persistence.outbox.SpringDataStoryOutboxRepository;
import com.planwith.planwith_fo_story.adapter.out.persistence.story.SpringDataStoryRepository;
import com.planwith.planwith_fo_story.application.command.CreateStoryCommandFactory;
import com.planwith.planwith_fo_story.application.event.StoryCreatedEvent;
import com.planwith.planwith_fo_story.application.port.in.StoryCommandUseCase;
import com.planwith.planwith_fo_story.application.query.StoryDetailView;
import com.planwith.planwith_fo_story.domain.exception.InvalidStoryStateException;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StoryCommandOutboxIntegrationTest {

	@Autowired
	private StoryCommandUseCase storyCommandUseCase;

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
	void createPersistsStoryAndOutboxInSameTransaction() {
		UUID memberUuid = UUID.randomUUID();

		StoryDetailView created = storyCommandUseCase.create(CreateStoryCommandFactory.basic(
				memberUuid,
				"첫 스토리",
				"본문입니다.",
				"https://img.example/cover.png",
				VisibilityScope.ALL
		));

		assertThat(created.storyUuid()).isNotBlank();
		assertThat(storyRepository.count()).isEqualTo(1);
		assertThat(outboxRepository.findAll()).singleElement().satisfies(outbox -> {
			assertThat(outbox.eventType()).isEqualTo(StoryCreatedEvent.EVENT_TYPE);
			assertThat(outbox.aggregateType()).isEqualTo("Story");
			assertThat(outbox.aggregateUuid().toString()).isEqualTo(created.storyUuid());
			assertThat(outbox.publishedAt()).isNull();
			assertThat(outbox.payload()).contains(created.storyUuid());
		});
	}

	@Test
	void createDoesNotLeaveStoryWhenDomainValidationFails() {
		UUID memberUuid = UUID.randomUUID();

		assertThatThrownBy(() -> storyCommandUseCase.create(CreateStoryCommandFactory.basic(
				memberUuid,
				" ",
				"본문입니다.",
				"https://img.example/cover.png",
				VisibilityScope.ALL
		))).isInstanceOf(InvalidStoryStateException.class);

		assertThat(storyRepository.count()).isZero();
		assertThat(outboxRepository.count()).isZero();
	}
}
