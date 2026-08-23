package com.planwith.planwith_fo_story.application.query;

import java.util.List;

public record StoryVisitCountryView(
		String countryName,
		int displayOrder,
		List<StoryVisitCityView> cities
) {
}
