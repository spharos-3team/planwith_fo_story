package com.planwith.planwith_fo_story.application.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_story.application.command.ChangeStoryCommentEnabledCommand;
import com.planwith.planwith_fo_story.application.command.ChangeStoryVisibilityCommand;
import com.planwith.planwith_fo_story.application.command.CreateStoryCommand;
import com.planwith.planwith_fo_story.application.command.DeleteStoryCommand;
import com.planwith.planwith_fo_story.application.command.UpdateStoryCommand;
import com.planwith.planwith_fo_story.application.event.StoryCreatedEvent;
import com.planwith.planwith_fo_story.application.event.StoryDeletedEvent;
import com.planwith.planwith_fo_story.application.event.StoryUpdatedEvent;
import com.planwith.planwith_fo_story.application.port.in.StoryCommandUseCase;
import com.planwith.planwith_fo_story.application.port.out.MemberProfileProjectionPort;
import com.planwith.planwith_fo_story.application.port.out.StoryCommandPort;
import com.planwith.planwith_fo_story.application.port.out.StoryEventOutboxPort;
import com.planwith.planwith_fo_story.application.port.out.StoryOutboxMessage;
import com.planwith.planwith_fo_story.application.port.out.StoryQueryCachePort;
import com.planwith.planwith_fo_story.application.query.StoryDetailView;
import com.planwith.planwith_fo_story.domain.exception.InvalidStoryStateException;
import com.planwith.planwith_fo_story.domain.exception.MemberAuthenticationRequiredException;
import com.planwith.planwith_fo_story.domain.exception.StoryNotFoundException;
import com.planwith.planwith_fo_story.domain.model.Story;
import com.planwith.planwith_fo_story.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_story.domain.model.vo.StoryUuid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoryCommandService implements StoryCommandUseCase {

	private final StoryCommandPort storyCommandPort;
	private final StoryEventOutboxPort storyEventOutboxPort;
	private final StoryQueryCachePort storyQueryCachePort;
	private final MemberProfileProjectionPort memberProfileProjectionPort;
	private final ObjectMapper objectMapper;
	private final Clock clock;

	@Override
	@Transactional
	public StoryDetailView create(CreateStoryCommand command) {
		log.info("StoryCommandService : create : 스토리 생성 비즈니스 로직 시작");
		requireActor(command.memberUuid());
		LocalDateTime now = LocalDateTime.now(clock);
		Story story = Story.create(
				StoryUuid.generate(),
				MemberUuid.of(command.memberUuid()),
				command.scheduleUuid(),
				command.scheduleVisible(),
				command.title(),
				command.content(),
				command.coverImageUrl(),
				command.startDate(),
				command.endDate(),
				command.commentEnabled(),
				command.visibilityScope(),
				now
		);
		Story saved = storyCommandPort.save(story);
		appendOutbox(StoryCreatedEvent.EVENT_TYPE, StoryCreatedEvent.of(
				UUID.randomUUID().toString(),
				saved.memberUuid().asString(),
				saved.storyUuid().asString(),
				Instant.now(clock)
		), saved);
		evictCaches(saved);
		log.info("StoryCommandService : create : 스토리 생성 완료 - storyUuid={}", saved.storyUuid());
		return toDetail(saved);
	}

	@Override
	@Transactional
	public StoryDetailView update(UpdateStoryCommand command) {
		log.info("StoryCommandService : update : 스토리 수정 비즈니스 로직 시작 - storyUuid={}", command.storyUuid());
		requireActor(command.actorUuid());
		LocalDateTime now = LocalDateTime.now(clock);
		Story updated = loadActive(command.storyUuid()).update(
				MemberUuid.of(command.actorUuid()),
				command.scheduleUuid(),
				command.scheduleVisible(),
				command.title(),
				command.content(),
				command.coverImageUrl(),
				command.startDate(),
				command.endDate(),
				now
		);
		Story saved = storyCommandPort.save(updated);
		appendOutbox(StoryUpdatedEvent.EVENT_TYPE, StoryUpdatedEvent.of(
				UUID.randomUUID().toString(),
				saved.memberUuid().asString(),
				saved.storyUuid().asString(),
				Instant.now(clock)
		), saved);
		evictCaches(saved);
		log.info("StoryCommandService : update : 스토리 수정 완료 - storyUuid={}", saved.storyUuid());
		return toDetail(saved);
	}

	@Override
	@Transactional
	public void delete(DeleteStoryCommand command) {
		log.info("StoryCommandService : delete : 스토리 삭제 비즈니스 로직 시작 - storyUuid={}", command.storyUuid());
		requireActor(command.actorUuid());
		Story deleted = loadActive(command.storyUuid())
				.delete(MemberUuid.of(command.actorUuid()), LocalDateTime.now(clock));
		Story saved = storyCommandPort.save(deleted);
		appendOutbox(StoryDeletedEvent.EVENT_TYPE, StoryDeletedEvent.of(
				UUID.randomUUID().toString(),
				saved.memberUuid().asString(),
				saved.storyUuid().asString(),
				Instant.now(clock)
		), saved);
		evictCaches(saved);
		log.info("StoryCommandService : delete : 스토리 삭제 완료 - storyUuid={}", saved.storyUuid());
	}

	@Override
	@Transactional
	public StoryDetailView changeVisibility(ChangeStoryVisibilityCommand command) {
		log.info("StoryCommandService : changeVisibility : 스토리 공개범위 변경 시작 - storyUuid={}", command.storyUuid());
		requireActor(command.actorUuid());
		Story changed = loadActive(command.storyUuid())
				.changeVisibility(MemberUuid.of(command.actorUuid()), command.visibilityScope(), LocalDateTime.now(clock));
		Story saved = storyCommandPort.save(changed);
		appendOutbox(StoryUpdatedEvent.EVENT_TYPE, StoryUpdatedEvent.of(
				UUID.randomUUID().toString(),
				saved.memberUuid().asString(),
				saved.storyUuid().asString(),
				Instant.now(clock)
		), saved);
		evictCaches(saved);
		log.info("StoryCommandService : changeVisibility : 스토리 공개범위 변경 완료 - storyUuid={}", saved.storyUuid());
		return toDetail(saved);
	}

	@Override
	@Transactional
	public StoryDetailView changeCommentEnabled(ChangeStoryCommentEnabledCommand command) {
		log.info("StoryCommandService : changeCommentEnabled : 스토리 댓글 허용 변경 시작 - storyUuid={}", command.storyUuid());
		requireActor(command.actorUuid());
		Story changed = loadActive(command.storyUuid())
				.changeCommentEnabled(MemberUuid.of(command.actorUuid()), command.commentEnabled(), LocalDateTime.now(clock));
		Story saved = storyCommandPort.save(changed);
		appendOutbox(StoryUpdatedEvent.EVENT_TYPE, StoryUpdatedEvent.of(
				UUID.randomUUID().toString(),
				saved.memberUuid().asString(),
				saved.storyUuid().asString(),
				Instant.now(clock)
		), saved);
		evictCaches(saved);
		log.info("StoryCommandService : changeCommentEnabled : 스토리 댓글 허용 변경 완료 - storyUuid={}", saved.storyUuid());
		return toDetail(saved);
	}

	private void requireActor(UUID actorUuid) {
		if (actorUuid == null) {
			throw new MemberAuthenticationRequiredException();
		}
	}

	private Story loadActive(UUID storyUuid) {
		Story story = storyCommandPort.findByStoryUuid(storyUuid)
				.orElseThrow(() -> new StoryNotFoundException(storyUuid.toString()));
		if (story.isDeleted()) {
			throw new StoryNotFoundException(storyUuid.toString());
		}
		return story;
	}

	private void appendOutbox(String eventType, Object payload, Story story) {
		storyEventOutboxPort.save(new StoryOutboxMessage(
				extractEventUuid(payload),
				StoryOutboxMessage.AGGREGATE_TYPE,
				story.storyUuid().asString(),
				eventType,
				writePayload(payload)
		));
	}

	private String extractEventUuid(Object payload) {
		if (payload instanceof StoryCreatedEvent event) {
			return event.eventUuid();
		}
		if (payload instanceof StoryUpdatedEvent event) {
			return event.eventUuid();
		}
		if (payload instanceof StoryDeletedEvent event) {
			return event.eventUuid();
		}
		return UUID.randomUUID().toString();
	}

	private String writePayload(Object payload) {
		try {
			return objectMapper.writeValueAsString(payload);
		} catch (JsonProcessingException exception) {
			throw new InvalidStoryStateException("스토리 이벤트 payload 직렬화에 실패했습니다.");
		}
	}

	private void evictCaches(Story story) {
		storyQueryCachePort.evictDetail(story.storyUuid().value());
		storyQueryCachePort.evictPopular();
		storyQueryCachePort.evictFeed(story.memberUuid().value());
	}

	private StoryDetailView toDetail(Story story) {
		return StoryViewMapper.toDetail(
				story,
				memberProfileProjectionPort.findByMemberUuid(story.memberUuid().value()).orElse(null)
		);
	}
}
