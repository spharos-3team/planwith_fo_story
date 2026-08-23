package com.planwith.planwith_fo_story.domain.exception;

public class StoryAccessDeniedException extends RuntimeException {

	public StoryAccessDeniedException() {
		super("스토리에 대한 권한이 없습니다.");
	}
}
