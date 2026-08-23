package com.planwith.planwith_fo_story.composition.application.query;

import java.time.LocalDateTime;

public record ScheduleFlightView(
		String airline,
		String flightNumber,
		LocalDateTime departureAt,
		LocalDateTime arrivalAt
) {
}
