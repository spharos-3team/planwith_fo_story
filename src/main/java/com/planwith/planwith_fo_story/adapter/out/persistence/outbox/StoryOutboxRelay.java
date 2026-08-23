package com.planwith.planwith_fo_story.adapter.out.persistence.outbox;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_story.application.event.StoryCreatedEvent;
import com.planwith.planwith_fo_story.application.event.StoryDeletedEvent;
import com.planwith.planwith_fo_story.application.event.StoryUpdatedEvent;
import com.planwith.planwith_fo_story.application.port.out.StoryEventPublisher;
import com.planwith.planwith_fo_story.config.StoryKafkaProperties;
import com.planwith.planwith_fo_story.config.StoryOutboxProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(name = "story.outbox.enabled", havingValue = "true")
public class StoryOutboxRelay {

	private final SpringDataStoryOutboxRepository repository;
	private final StoryEventPublisher publisher;
	private final StoryOutboxProperties outboxProperties;
	private final StoryKafkaProperties kafkaProperties;
	private final Clock clock;

	public StoryOutboxRelay(
			SpringDataStoryOutboxRepository repository,
			StoryEventPublisher publisher,
			StoryOutboxProperties outboxProperties,
			StoryKafkaProperties kafkaProperties,
			Clock clock
	) {
		this.repository = repository;
		this.publisher = publisher;
		this.outboxProperties = outboxProperties;
		this.kafkaProperties = kafkaProperties;
		this.clock = clock;
	}

	@Scheduled(
			fixedDelayString = "${story.outbox.relay-interval:5s}",
			initialDelayString = "${story.outbox.relay-initial-delay:5s}"
	)
	@Transactional
	public void relayUnpublishedEvents() {
		int batchSize = outboxProperties.getRelayBatchSize() > 0
				? outboxProperties.getRelayBatchSize()
				: 50;
		Instant now = clock.instant();
		List<StoryOutboxJpaEntity> unpublished = repository.findDueUnpublished(now, PageRequest.of(0, batchSize));
		for (StoryOutboxJpaEntity outbox : unpublished) {
			if (outbox.isDue(now)) {
				publish(outbox, now);
			}
		}
	}

	private void publish(StoryOutboxJpaEntity outbox, Instant now) {
		try {
			publisher.publish(
							topicFor(outbox.eventType()),
							outbox.aggregateUuid().toString(),
							outbox.payload()
					)
					.get(sendTimeoutMillis(), TimeUnit.MILLISECONDS);
			outbox.markPublished(now);
			log.info("StoryOutboxRelay : publish : 스토리 Outbox 발행 완료 - eventUuid={}, eventType={}",
					outbox.eventUuid(), outbox.eventType());
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			recordFailure(outbox, now);
			log.warn("StoryOutboxRelay : publish : 스토리 Outbox 발행 중단 - eventUuid={}, retryCount={}",
					outbox.eventUuid(), outbox.retryCount());
		} catch (Exception exception) {
			recordFailure(outbox, now);
			if (outboxProperties.retryLimitReached(outbox.retryCount())) {
				log.error(
						"StoryOutboxRelay : publish : 스토리 Outbox 최대 재시도 이후에도 미발행 유지 - eventUuid={}, retryCount={}",
						outbox.eventUuid(),
						outbox.retryCount()
				);
			} else {
				log.warn("StoryOutboxRelay : publish : 스토리 Outbox 발행 실패 - eventUuid={}, retryCount={}",
						outbox.eventUuid(), outbox.retryCount());
			}
		}
	}

	private void recordFailure(StoryOutboxJpaEntity outbox, Instant now) {
		int nextRetryCount = outbox.retryCount() + 1;
		outbox.recordPublishFailure(outboxProperties.nextRetryAt(now, nextRetryCount));
	}

	private String topicFor(String eventType) {
		if (StoryUpdatedEvent.EVENT_TYPE.equals(eventType)) {
			return kafkaProperties.getTopics().getStoryUpdated();
		}
		if (StoryDeletedEvent.EVENT_TYPE.equals(eventType)) {
			return kafkaProperties.getTopics().getStoryDeleted();
		}
		if (!StoryCreatedEvent.EVENT_TYPE.equals(eventType)) {
			log.warn("StoryOutboxRelay : topicFor : 알 수 없는 Outbox eventType이라 story.created로 발행 - eventType={}",
					eventType);
		}
		return kafkaProperties.getTopics().getStoryCreated();
	}

	private long sendTimeoutMillis() {
		Duration timeout = outboxProperties.getSendTimeout();
		if (timeout == null || timeout.isZero() || timeout.isNegative()) {
			return Duration.ofSeconds(10).toMillis();
		}
		return timeout.toMillis();
	}
}
