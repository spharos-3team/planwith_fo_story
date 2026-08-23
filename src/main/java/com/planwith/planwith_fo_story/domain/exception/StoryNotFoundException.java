package com.planwith.planwith_fo_story.domain.exception;

public class StoryNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public StoryNotFoundException(String storyUuid) {
		super("스토리를 찾을 수 없습니다. storyUuid=" + storyUuid);
	}
}
