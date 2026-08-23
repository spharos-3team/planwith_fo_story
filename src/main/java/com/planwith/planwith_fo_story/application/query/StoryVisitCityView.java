package com.planwith.planwith_fo_story.application.query;

import java.util.List;

public record StoryVisitCityView(
		String cityName,
		int displayOrder,
		List<StoryPlaceView> places
) {
}
