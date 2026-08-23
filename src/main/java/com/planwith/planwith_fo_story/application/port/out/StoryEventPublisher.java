package com.planwith.planwith_fo_story.application.port.out;

import java.util.concurrent.CompletableFuture;

public interface StoryEventPublisher {

	CompletableFuture<Void> publish(String topic, String key, String payload);
}
