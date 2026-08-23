package com.planwith.planwith_fo_story.domain.exception;

public class MemberAuthenticationRequiredException extends RuntimeException {

	public MemberAuthenticationRequiredException() {
		super("로그인한 회원만 스토리를 작성할 수 있습니다.");
	}
}
