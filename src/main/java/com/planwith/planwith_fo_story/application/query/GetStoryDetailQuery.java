package com.planwith.planwith_fo_story.application.query;

import java.util.UUID;

public record GetStoryDetailQuery(
		UUID storyUuid,
		UUID viewerUuid
) {
}
