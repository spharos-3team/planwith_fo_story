package com.planwith.planwith_fo_story.adapter.out.persistence.outbox;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_story.application.port.out.StoryEventOutboxPort;
import com.planwith.planwith_fo_story.application.port.out.StoryOutboxMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoryEventOutboxAdapter implements StoryEventOutboxPort {

	private final SpringDataStoryOutboxRepository repository;

	@Override
	@Transactional
	public void save(StoryOutboxMessage message) {
		UUID eventUuid = UUID.fromString(message.eventUuid());
		if (repository.existsByEventUuid(eventUuid)) {
			log.warn("StoryEventOutboxAdapter : save : 중복 Outbox 이벤트 저장 생략 - eventUuid={}",
					message.eventUuid());
			return;
		}
		repository.save(new StoryOutboxJpaEntity(
				eventUuid,
				message.aggregateType(),
				UUID.fromString(message.aggregateUuid()),
				message.eventType(),
				message.payload(),
				Instant.now()
		));
		log.info("StoryEventOutboxAdapter : save : 스토리 Outbox 저장 완료 - eventUuid={}, eventType={}",
				message.eventUuid(), message.eventType());
	}
}
