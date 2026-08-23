package com.planwith.planwith_fo_story.composition.adapter.out.schedule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.planwith.planwith_fo_story.composition.application.port.out.ScheduleDetailQueryPort;
import com.planwith.planwith_fo_story.composition.application.query.ScheduleDailyPlanView;
import com.planwith.planwith_fo_story.composition.application.query.ScheduleFlightView;
import com.planwith.planwith_fo_story.composition.application.query.StoryDetailScreenView.ScheduleScreenView;
import com.planwith.planwith_fo_story.composition.config.StoryDetailScreenProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "story.detail-screen.schedule", name = "query-enabled", havingValue = "true")
public class RestScheduleDetailQueryAdapter implements ScheduleDetailQueryPort {

	private final RestClient restClient;
	private final StoryDetailScreenProperties properties;

	public RestScheduleDetailQueryAdapter(RestClient.Builder restClientBuilder, StoryDetailScreenProperties properties) {
		this.properties = properties;
		this.restClient = restClientBuilder.baseUrl(properties.getSchedule().getBaseUrl()).build();
	}

	@Override
	public Optional<ScheduleScreenView> findByScheduleUuid(UUID scheduleUuid) {
		log.info(
				"RestScheduleDetailQueryAdapter : findByScheduleUuid : Schedule Service 상세 조회 시작 - scheduleUuid={}",
				scheduleUuid
		);
		try {
			ScheduleDetailResponse response = restClient.get()
					.uri(properties.getSchedule().getDetailPath(), scheduleUuid)
					.retrieve()
					.body(ScheduleDetailResponse.class);
			if (response == null) {
				return Optional.empty();
			}
			log.info(
					"RestScheduleDetailQueryAdapter : findByScheduleUuid : Schedule Service 상세 조회 완료 - scheduleUuid={}",
					scheduleUuid
			);
			return Optional.of(new ScheduleScreenView(
					response.scheduleUuid(),
					response.title(),
					mapDailyPlans(response.dailyPlans()),
					mapFlights(response.flights())
			));
		} catch (RestClientException exception) {
			log.warn(
					"RestScheduleDetailQueryAdapter : findByScheduleUuid : Schedule Service 상세 조회 실패 - scheduleUuid={}",
					scheduleUuid
			);
			return Optional.empty();
		}
	}

	private static List<ScheduleDailyPlanView> mapDailyPlans(List<DailyPlanResponse> dailyPlans) {
		if (dailyPlans == null) {
			return List.of();
		}
		return dailyPlans.stream()
				.map(plan -> new ScheduleDailyPlanView(plan.date(), plan.title(), plan.items() == null ? List.of() : plan.items()))
				.toList();
	}

	private static List<ScheduleFlightView> mapFlights(List<FlightResponse> flights) {
		if (flights == null) {
			return List.of();
		}
		return flights.stream()
				.map(flight -> new ScheduleFlightView(
						flight.airline(),
						flight.flightNumber(),
						flight.departureAt(),
						flight.arrivalAt()
				))
				.toList();
	}

	public record ScheduleDetailResponse(
			String scheduleUuid,
			String title,
			List<DailyPlanResponse> dailyPlans,
			List<FlightResponse> flights
	) {
	}

	public record DailyPlanResponse(LocalDate date, String title, List<String> items) {
	}

	public record FlightResponse(
			String airline,
			String flightNumber,
			LocalDateTime departureAt,
			LocalDateTime arrivalAt
	) {
	}
}
