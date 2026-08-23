package com.planwith.planwith_fo_story.composition.application.query;

import java.time.LocalDate;
import java.util.List;

public record ScheduleDailyPlanView(
		LocalDate date,
		String title,
		List<String> items
) {
}
