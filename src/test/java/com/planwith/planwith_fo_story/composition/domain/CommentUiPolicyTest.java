package com.planwith.planwith_fo_story.composition.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class CommentUiPolicyTest {

	@Test
	void returnsDisabledWhenCommentDisabled() {
		assertThat(CommentUiPolicy.resolve(false, UUID.randomUUID())).isEqualTo(CommentUiState.DISABLED);
		assertThat(CommentUiPolicy.resolve(false, null)).isEqualTo(CommentUiState.DISABLED);
	}

	@Test
	void returnsLoginRequiredWhenCommentEnabledAndGuest() {
		assertThat(CommentUiPolicy.resolve(true, null)).isEqualTo(CommentUiState.LOGIN_REQUIRED);
	}

	@Test
	void returnsCommentUiWhenCommentEnabledAndLoggedIn() {
		assertThat(CommentUiPolicy.resolve(true, UUID.randomUUID())).isEqualTo(CommentUiState.COMMENT_UI);
	}
}
