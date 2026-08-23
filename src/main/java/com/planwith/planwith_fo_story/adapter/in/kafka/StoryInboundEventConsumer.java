package com.planwith.planwith_fo_story.adapter.in.kafka;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_story.adapter.in.kafka.dto.LikeChangedEventPayload;
import com.planwith.planwith_fo_story.adapter.in.kafka.dto.CommentChangedEventPayload;
import com.planwith.planwith_fo_story.adapter.in.kafka.dto.MemberProfileChangedEventPayload;
import com.planwith.planwith_fo_story.adapter.in.kafka.dto.MembershipChangedEventPayload;
import com.planwith.planwith_fo_story.application.command.ProjectLikeCountCommand;
import com.planwith.planwith_fo_story.application.command.ProjectCommentCountCommand;
import com.planwith.planwith_fo_story.application.command.ProjectMemberProfileCommand;
import com.planwith.planwith_fo_story.application.command.ProjectMembershipEntitlementCommand;
import com.planwith.planwith_fo_story.application.port.in.StoryProjectionUseCase;
import com.planwith.planwith_fo_story.config.StoryKafkaProperties;
import com.planwith.planwith_fo_story.domain.model.MemberStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(name = "story.kafka.consumer-enabled", havingValue = "true")
public class StoryInboundEventConsumer {

	private final StoryProjectionUseCase storyProjectionUseCase;
	private final ObjectMapper objectMapper;
	private final StoryKafkaProperties.Topics topics;

	public StoryInboundEventConsumer(
			StoryProjectionUseCase storyProjectionUseCase,
			ObjectMapper objectMapper,
			StoryKafkaProperties kafkaProperties
	) {
		this.storyProjectionUseCase = storyProjectionUseCase;
		this.objectMapper = objectMapper;
		this.topics = kafkaProperties.getTopics();
	}

	@KafkaListener(
			topics = {
					"${story.kafka.topics.member-profile-changed}",
					"${story.kafka.topics.like-created}",
					"${story.kafka.topics.like-removed}",
					"${story.kafka.topics.comment-created}",
					"${story.kafka.topics.comment-removed}",
					"${story.kafka.topics.membership-subscribed}",
					"${story.kafka.topics.membership-canceled}"
			}
	)
	public void consume(
			@Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
			@Payload String payload
	) {
		log.info("StoryInboundEventConsumer : consume : 스토리 입력 이벤트 수신 - topic={}", topic);
		try {
			if (topics.getMemberProfileChanged().equals(topic)) {
				projectMemberProfile(payload);
				return;
			}
			if (topics.getLikeCreated().equals(topic)) {
				projectLike(payload, true);
				return;
			}
			if (topics.getLikeRemoved().equals(topic)) {
				projectLike(payload, false);
				return;
			}
			if (topics.getCommentCreated().equals(topic)) {
				projectComment(payload, true);
				return;
			}
			if (topics.getCommentRemoved().equals(topic)) {
				projectComment(payload, false);
				return;
			}
			if (topics.getMembershipSubscribed().equals(topic)) {
				projectMembership(payload, true);
				return;
			}
			if (topics.getMembershipCanceled().equals(topic)) {
				projectMembership(payload, false);
				return;
			}
			log.warn("StoryInboundEventConsumer : consume : 지원하지 않는 Projection 토픽 - topic={}", topic);
		} catch (IllegalArgumentException exception) {
			log.error("StoryInboundEventConsumer : consume : 잘못된 입력 이벤트로 Projection 생략 - topic={}", topic);
		} catch (RuntimeException exception) {
			log.error("StoryInboundEventConsumer : consume : 입력 이벤트 처리 실패로 재처리 대기 - topic={}", topic);
			throw exception;
		}
	}

	private void projectMemberProfile(String payload) {
		MemberProfileChangedEventPayload event = parse(payload, MemberProfileChangedEventPayload.class);
		if (event == null || event.memberUuid() == null) {
			return;
		}
		storyProjectionUseCase.projectMemberProfile(new ProjectMemberProfileCommand(
				UUID.fromString(event.memberUuid()),
				event.nickname(),
				event.profileImage(),
				parseMemberStatus(event.memberStatus()),
				event.sourceVersion() == null ? 0L : event.sourceVersion()
		));
	}

	private void projectLike(String payload, boolean created) {
		LikeChangedEventPayload event = parse(payload, LikeChangedEventPayload.class);
		if (event == null || event.targetUuid() == null) {
			return;
		}
		ProjectLikeCountCommand command = new ProjectLikeCountCommand(
				event.targetType(),
				UUID.fromString(event.targetUuid()),
				event.sourceVersion() == null ? 0L : event.sourceVersion()
		);
		if (created) {
			storyProjectionUseCase.projectLikeCreated(command);
			return;
		}
		storyProjectionUseCase.projectLikeRemoved(command);
	}

	private void projectComment(String payload, boolean created) {
		CommentChangedEventPayload event = parse(payload, CommentChangedEventPayload.class);
		if (event == null || event.targetUuid() == null) {
			return;
		}
		ProjectCommentCountCommand command = new ProjectCommentCountCommand(
				event.targetType(),
				UUID.fromString(event.targetUuid()),
				event.sourceVersion() == null ? 0L : event.sourceVersion()
		);
		if (created) {
			storyProjectionUseCase.projectCommentCreated(command);
			return;
		}
		storyProjectionUseCase.projectCommentRemoved(command);
	}

	private void projectMembership(String payload, boolean subscribed) {
		MembershipChangedEventPayload event = parse(payload, MembershipChangedEventPayload.class);
		if (event == null || event.memberUuid() == null || event.creatorUuid() == null) {
			return;
		}
		ProjectMembershipEntitlementCommand command = new ProjectMembershipEntitlementCommand(
				UUID.fromString(event.memberUuid()),
				UUID.fromString(event.creatorUuid()),
				event.membershipUuid() == null ? null : UUID.fromString(event.membershipUuid()),
				event.sourceVersion() == null ? 0L : event.sourceVersion()
		);
		if (subscribed) {
			storyProjectionUseCase.projectMembershipSubscribed(command);
			return;
		}
		storyProjectionUseCase.projectMembershipCanceled(command);
	}

	private MemberStatus parseMemberStatus(String memberStatus) {
		if (memberStatus == null || memberStatus.isBlank()) {
			return MemberStatus.ACTIVE;
		}
		return MemberStatus.valueOf(memberStatus);
	}

	private <T> T parse(String payload, Class<T> type) {
		if (payload == null || payload.isBlank()) {
			return null;
		}
		try {
			return objectMapper.readValue(payload, type);
		} catch (JsonProcessingException exception) {
			log.error("StoryInboundEventConsumer : consume : 입력 이벤트 JSON 파싱 실패 - payloadType={}",
					type.getSimpleName());
			return null;
		}
	}
}
