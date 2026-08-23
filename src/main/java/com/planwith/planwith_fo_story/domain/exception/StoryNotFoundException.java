package com.planwith.planwith_fo_story.domain.exception;

public class StoryNotFoundException extends RuntimeException {

	public StoryNotFoundException(String storyUuid) {
		super("스토리를 찾을 수 없습니다. storyUuid=" + storyUuid);
	}
}
