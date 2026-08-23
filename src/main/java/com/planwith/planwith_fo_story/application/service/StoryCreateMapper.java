package com.planwith.planwith_fo_story.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.planwith.planwith_fo_story.application.command.CreateStoryCommand;
import com.planwith.planwith_fo_story.domain.model.Story;
import com.planwith.planwith_fo_story.domain.model.StoryPlace;
import com.planwith.planwith_fo_story.domain.model.StoryPlaceImage;
import com.planwith.planwith_fo_story.domain.model.StoryTag;
import com.planwith.planwith_fo_story.domain.model.StoryVisibilityMember;
import com.planwith.planwith_fo_story.domain.model.StoryVisitCity;
import com.planwith.planwith_fo_story.domain.model.StoryVisitCountry;
import com.planwith.planwith_fo_story.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_story.domain.model.vo.StoryUuid;

final class StoryCreateMapper {

	private StoryCreateMapper() {
	}

	static Story toNewStory(CreateStoryCommand command, LocalDateTime now) {
		Story story = Story.create(
				StoryUuid.generate(),
				MemberUuid.of(command.memberUuid()),
				command.scheduleUuid(),
				command.scheduleVisible(),
				command.title(),
				command.content(),
				command.coverImageUrl(),
				command.startDate(),
				command.endDate(),
				command.commentEnabled(),
				command.visibilityScope(),
				now
		);
		return story.replaceChildren(
				toCountries(command.countries(), now),
				toTags(command.tags()),
				toVisibilityMembers(command.visibilityMemberUuids(), now),
				now
		);
	}

	private static List<StoryVisitCountry> toCountries(List<CreateStoryCommand.Country> countries, LocalDateTime now) {
		return countries.stream()
				.map(country -> StoryVisitCountry.create(
						country.countryName(),
						country.displayOrder(),
						country.cities().stream()
								.map(city -> StoryVisitCity.create(
										city.cityName(),
										city.displayOrder(),
										toPlaces(city.places(), now)
								))
								.toList()
				))
				.toList();
	}

	private static List<StoryPlace> toPlaces(List<CreateStoryCommand.Place> places, LocalDateTime now) {
		return places.stream()
				.map(place -> StoryPlace.create(
						null,
						place.placeName(),
						place.displayOrder(),
						place.images().stream()
								.map(image -> StoryPlaceImage.create(image.imageUrl(), image.imageOrder(), now))
								.toList()
				))
				.toList();
	}

	private static List<StoryTag> toTags(List<String> tags) {
		return tags.stream().map(StoryTag::create).toList();
	}

	private static List<StoryVisibilityMember> toVisibilityMembers(List<UUID> memberUuids, LocalDateTime now) {
		return memberUuids.stream()
				.map(memberUuid -> StoryVisibilityMember.create(MemberUuid.of(memberUuid), now))
				.toList();
	}
}
