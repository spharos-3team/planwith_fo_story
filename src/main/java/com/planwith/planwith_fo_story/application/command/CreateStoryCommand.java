package com.planwith.planwith_fo_story.application.command;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.planwith.planwith_fo_story.domain.model.VisibilityScope;

public record CreateStoryCommand(
		UUID memberUuid,
		UUID scheduleUuid,
		boolean scheduleVisible,
		String title,
		String content,
		String coverImageUrl,
		LocalDate startDate,
		LocalDate endDate,
		boolean commentEnabled,
		VisibilityScope visibilityScope,
		boolean aiVerificationRequested,
		List<Country> countries,
		List<String> tags,
		List<UUID> visibilityMemberUuids
) {
	public CreateStoryCommand {
		countries = countries == null ? List.of() : List.copyOf(countries);
		tags = tags == null ? List.of() : List.copyOf(tags);
		visibilityMemberUuids = visibilityMemberUuids == null ? List.of() : List.copyOf(visibilityMemberUuids);
	}

	public record Country(
			String countryName,
			int displayOrder,
			List<City> cities
	) {
		public Country {
			cities = cities == null ? List.of() : List.copyOf(cities);
		}
	}

	public record City(
			String cityName,
			int displayOrder,
			List<Place> places
	) {
		public City {
			places = places == null ? List.of() : List.copyOf(places);
		}
	}

	public record Place(
			String placeName,
			int displayOrder,
			List<PlaceImage> images
	) {
		public Place {
			images = images == null ? List.of() : List.copyOf(images);
		}
	}

	public record PlaceImage(
			String imageUrl,
			int imageOrder
	) {
	}
}
