package com.planwith.planwith_fo_story.domain.exception;

public class InvalidStoryQueryException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidStoryQueryException(String message) {
		super(message);
	}
}
