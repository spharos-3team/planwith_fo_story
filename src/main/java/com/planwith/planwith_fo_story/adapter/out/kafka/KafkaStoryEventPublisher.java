package com.planwith.planwith_fo_story.adapter.out.kafka;

import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_story.application.port.out.StoryEventPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaStoryEventPublisher implements StoryEventPublisher {

	private final KafkaTemplate<String, String> kafkaTemplate;

	@Override
	public CompletableFuture<Void> publish(String topic, String key, String payload) {
		log.info("KafkaStoryEventPublisher : publish : 스토리 이벤트 Kafka 발행 시작 - topic={}, key={}", topic, key);
		return kafkaTemplate.send(topic, key, payload)
				.thenAccept(result -> log.info(
						"KafkaStoryEventPublisher : publish : 스토리 이벤트 Kafka 발행 완료 - topic={}, key={}",
						topic,
						key
				));
	}
}
