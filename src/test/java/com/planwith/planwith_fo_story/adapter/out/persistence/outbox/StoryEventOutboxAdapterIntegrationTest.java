package com.planwith.planwith_fo_story.adapter.out.persistence.outbox;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_story.application.event.StoryCreatedEvent;
import com.planwith.planwith_fo_story.application.port.out.StoryEventOutboxPort;
import com.planwith.planwith_fo_story.application.port.out.StoryOutboxMessage;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StoryEventOutboxAdapterIntegrationTest {

	@Autowired
	private StoryEventOutboxPort outboxPort;

	@Autowired
	private SpringDataStoryOutboxRepository repository;

	@BeforeEach
	void clearOutbox() {
		repository.deleteAll();
	}

	@Test
	void storesOnlyOneOutboxRecordForSameEventUuid() {
		String eventUuid = UUID.randomUUID().toString();
		StoryOutboxMessage message = new StoryOutboxMessage(
				eventUuid,
				StoryOutboxMessage.AGGREGATE_TYPE,
				UUID.randomUUID().toString(),
				StoryCreatedEvent.EVENT_TYPE,
				"{\"storyUuid\":\"story-uuid\"}"
		);

		outboxPort.save(message);
		outboxPort.save(message);

		assertThat(repository.findAll()).singleElement().satisfies(outbox -> {
			assertThat(outbox.eventUuid()).isEqualTo(UUID.fromString(eventUuid));
			assertThat(outbox.eventType()).isEqualTo(StoryCreatedEvent.EVENT_TYPE);
			assertThat(outbox.publishedAt()).isNull();
			assertThat(outbox.retryCount()).isZero();
		});
		assertThat(repository.findUnpublished(PageRequest.of(0, 10))).hasSize(1);
		assertThat(repository.findDueUnpublished(Instant.now(), PageRequest.of(0, 10))).hasSize(1);
	}
}
