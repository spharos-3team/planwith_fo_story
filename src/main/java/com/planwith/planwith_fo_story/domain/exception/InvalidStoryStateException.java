package com.planwith.planwith_fo_story.domain.exception;

public class InvalidStoryStateException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidStoryStateException(String message) {
		super(message);
	}
}
