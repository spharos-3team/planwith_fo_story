package com.planwith.planwith_fo_story.application.query;

import java.util.UUID;

public record GetMyStoryDetailQuery(
		UUID memberUuid,
		UUID storyUuid
) {
}
