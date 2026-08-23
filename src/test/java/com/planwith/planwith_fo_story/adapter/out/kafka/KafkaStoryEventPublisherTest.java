package com.planwith.planwith_fo_story.adapter.out.kafka;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

class KafkaStoryEventPublisherTest {

	@Test
	void publishesPayloadToKafkaTopic() {
		@SuppressWarnings("unchecked")
		KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
		when(kafkaTemplate.send("planwith.story.created", "story-uuid", "{}"))
				.thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

		KafkaStoryEventPublisher publisher = new KafkaStoryEventPublisher(kafkaTemplate);
		publisher.publish("planwith.story.created", "story-uuid", "{}");

		verify(kafkaTemplate).send("planwith.story.created", "story-uuid", "{}");
	}
}
