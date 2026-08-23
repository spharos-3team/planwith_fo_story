package com.planwith.planwith_fo_story.composition.application.query;

import java.time.LocalDateTime;

public record StoryCommentItemView(
		String commentUuid,
		String memberUuid,
		String content,
		LocalDateTime createdAt
) {
}
