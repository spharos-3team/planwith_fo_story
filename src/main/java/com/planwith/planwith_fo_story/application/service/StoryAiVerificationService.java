package com.planwith.planwith_fo_story.application.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_story.application.event.StoryAiUsageRecordedEvent;
import com.planwith.planwith_fo_story.application.port.out.StoryAiModerationPort;
import com.planwith.planwith_fo_story.application.port.out.StoryAiModerationResult;
import com.planwith.planwith_fo_story.application.port.out.StoryAiVerificationRequestPort;
import com.planwith.planwith_fo_story.application.port.out.StoryCommandPort;
import com.planwith.planwith_fo_story.application.port.out.StoryEventOutboxPort;
import com.planwith.planwith_fo_story.application.port.out.StoryOutboxMessage;
import com.planwith.planwith_fo_story.application.port.out.StoryQueryCachePort;
import com.planwith.planwith_fo_story.domain.exception.InvalidStoryStateException;
import com.planwith.planwith_fo_story.domain.model.AiModerationStatus;
import com.planwith.planwith_fo_story.domain.model.Story;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class StoryAiVerificationService implements StoryAiVerificationRequestPort {

	private final StoryCommandPort storyCommandPort;
	private final StoryAiModerationPort storyAiModerationPort;
	private final StoryEventOutboxPort storyEventOutboxPort;
	private final StoryQueryCachePort storyQueryCachePort;
	private final ObjectMapper objectMapper;
	private final Clock clock;
	private final TransactionTemplate transactionTemplate;

	public StoryAiVerificationService(
			StoryCommandPort storyCommandPort,
			StoryAiModerationPort storyAiModerationPort,
			StoryEventOutboxPort storyEventOutboxPort,
			StoryQueryCachePort storyQueryCachePort,
			ObjectMapper objectMapper,
			Clock clock,
			PlatformTransactionManager transactionManager
	) {
		this.storyCommandPort = storyCommandPort;
		this.storyAiModerationPort = storyAiModerationPort;
		this.storyEventOutboxPort = storyEventOutboxPort;
		this.storyQueryCachePort = storyQueryCachePort;
		this.objectMapper = objectMapper;
		this.clock = clock;
		this.transactionTemplate = new TransactionTemplate(transactionManager);
		this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
	}

	@Override
	public void requestVerification(UUID storyUuid, UUID memberUuid) {
		log.info("StoryAiVerificationService : requestVerification : AI 검증 비즈니스 로직 시작 - storyUuid={}", storyUuid);
		Story story = storyCommandPort.findByStoryUuid(storyUuid).orElse(null);
		if (story == null || story.isDeleted()) {
			log.warn("StoryAiVerificationService : requestVerification : 검증 대상 스토리를 찾을 수 없음 - storyUuid={}", storyUuid);
			return;
		}
		storyAiModerationPort.moderate(story.title(), story.content())
				.ifPresentOrElse(
						result -> applyResult(storyUuid, memberUuid, result),
						() -> log.info(
								"StoryAiVerificationService : requestVerification : AI 검증 실패로 UNVERIFIED 유지 - storyUuid={}",
								storyUuid
						)
				);
	}

	private void applyResult(UUID storyUuid, UUID memberUuid, StoryAiModerationResult result) {
		String requestId = UUID.randomUUID().toString();
		AiModerationStatus status = result.verified() ? AiModerationStatus.VERIFIED : AiModerationStatus.UNVERIFIED;
		transactionTemplate.executeWithoutResult(transactionStatus -> persistResult(
				storyUuid,
				memberUuid,
				requestId,
				status,
				result
		));
		log.info(
				"StoryAiVerificationService : requestVerification : AI 검증 반영 완료 - storyUuid={}, status={}, requestId={}",
				storyUuid,
				status,
				requestId
		);
	}

	private void persistResult(
			UUID storyUuid,
			UUID memberUuid,
			String requestId,
			AiModerationStatus status,
			StoryAiModerationResult result
	) {
		Story saved = storyCommandPort.updateAiModerationStatus(storyUuid, status, LocalDateTime.now(clock))
				.orElse(null);
		if (saved == null) {
			log.warn("StoryAiVerificationService : persistResult : 검증 결과 저장 전 스토리가 없음 - storyUuid={}", storyUuid);
			return;
		}
		appendUsageOutbox(requestId, memberUuid, saved, result);
		storyQueryCachePort.evictDetail(saved.storyUuid().value());
		storyQueryCachePort.evictPopular();
		storyQueryCachePort.evictFeed(saved.memberUuid().value());
	}

	private void appendUsageOutbox(
			String requestId,
			UUID memberUuid,
			Story story,
			StoryAiModerationResult result
	) {
		StoryAiUsageRecordedEvent event = StoryAiUsageRecordedEvent.of(
				requestId,
				memberUuid.toString(),
				story.storyUuid().asString(),
				result.inputTokens(),
				result.outputTokens(),
				result.totalTokens(),
				result.model(),
				Instant.now(clock)
		);
		try {
			storyEventOutboxPort.save(new StoryOutboxMessage(
					requestId,
					StoryOutboxMessage.AGGREGATE_TYPE,
					story.storyUuid().asString(),
					StoryAiUsageRecordedEvent.EVENT_TYPE,
					objectMapper.writeValueAsString(event)
			));
		} catch (JsonProcessingException exception) {
			throw new InvalidStoryStateException("스토리 AI Usage 이벤트 payload 직렬화에 실패했습니다.");
		}
	}
}
