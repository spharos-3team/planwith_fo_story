package com.planwith.planwith_fo_story.adapter.in.kafka;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_story.application.command.ProjectLikeCountCommand;
import com.planwith.planwith_fo_story.application.command.ProjectMemberProfileCommand;
import com.planwith.planwith_fo_story.application.port.in.StoryProjectionUseCase;
import com.planwith.planwith_fo_story.config.StoryKafkaProperties;

class StoryInboundEventConsumerTest {

	private StoryProjectionUseCase projectionUseCase;
	private StoryInboundEventConsumer consumer;
	private final StoryKafkaProperties properties = new StoryKafkaProperties();

	@BeforeEach
	void setUp() {
		projectionUseCase = mock(StoryProjectionUseCase.class);
		consumer = new StoryInboundEventConsumer(projectionUseCase, new ObjectMapper(), properties);
	}

	@Test
	void projectsMemberProfileChanged() {
		UUID memberUuid = UUID.randomUUID();
		String payload = """
				{"eventUuid":"%s","memberUuid":"%s","nickname":"여행자","memberStatus":"ACTIVE","sourceVersion":3}
				""".formatted(UUID.randomUUID(), memberUuid);

		consumer.consume(properties.getTopics().getMemberProfileChanged(), payload);

		verify(projectionUseCase).projectMemberProfile(any(ProjectMemberProfileCommand.class));
	}

	@Test
	void projectsLikeCreatedForStoryTarget() {
		UUID storyUuid = UUID.randomUUID();
		String payload = """
				{"eventUuid":"%s","targetType":"STORY","targetUuid":"%s","sourceVersion":1}
				""".formatted(UUID.randomUUID(), storyUuid);

		consumer.consume(properties.getTopics().getLikeCreated(), payload);

		verify(projectionUseCase).projectLikeCreated(any(ProjectLikeCountCommand.class));
	}

	@Test
	void ignoresBlankPayload() {
		consumer.consume(properties.getTopics().getLikeRemoved(), " ");

		verify(projectionUseCase, never()).projectLikeRemoved(any());
	}
}
