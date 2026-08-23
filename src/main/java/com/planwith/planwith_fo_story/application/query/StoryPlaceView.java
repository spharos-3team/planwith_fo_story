package com.planwith.planwith_fo_story.application.query;

import java.util.List;

public record StoryPlaceView(
		String placeName,
		int displayOrder,
		List<StoryPlaceImageView> images
) {
}
