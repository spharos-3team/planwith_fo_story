package com.planwith.planwith_fo_story.composition.domain;

import java.util.UUID;

public final class CommentUiPolicy {

	public static final String DISABLED_MESSAGE = "댓글 사용이 중지되었습니다.";

	private CommentUiPolicy() {
	}

	public static CommentUiState resolve(boolean commentEnabled, UUID viewerUuid) {
		if (!commentEnabled) {
			return CommentUiState.DISABLED;
		}
		if (viewerUuid == null) {
			return CommentUiState.LOGIN_REQUIRED;
		}
		return CommentUiState.COMMENT_UI;
	}
}
