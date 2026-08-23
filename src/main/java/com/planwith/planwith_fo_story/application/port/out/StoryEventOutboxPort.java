package com.planwith.planwith_fo_story.application.port.out;

public interface StoryEventOutboxPort {

	void save(StoryOutboxMessage message);
}
