package com.planwith.planwith_fo_story.domain.exception;

public class ScheduleNotOwnedException extends RuntimeException {

	public ScheduleNotOwnedException() {
		super("본인 일정만 스토리에 첨부할 수 있습니다.");
	}
}
