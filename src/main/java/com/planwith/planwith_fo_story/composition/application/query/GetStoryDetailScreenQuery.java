package com.planwith.planwith_fo_story.composition.application.query;

import java.util.UUID;

public record GetStoryDetailScreenQuery(
		UUID storyUuid,
		UUID viewerUuid
) {
}
