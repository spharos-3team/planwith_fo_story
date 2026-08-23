package com.planwith.planwith_fo_story.adapter.out.persistence.story;

import java.util.List;

import com.planwith.planwith_fo_story.domain.model.Story;
import com.planwith.planwith_fo_story.domain.model.StoryPlace;
import com.planwith.planwith_fo_story.domain.model.StoryPlaceImage;
import com.planwith.planwith_fo_story.domain.model.StoryTag;
import com.planwith.planwith_fo_story.domain.model.StoryVisibilityMember;
import com.planwith.planwith_fo_story.domain.model.StoryVisitCity;
import com.planwith.planwith_fo_story.domain.model.StoryVisitCountry;
import com.planwith.planwith_fo_story.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_story.domain.model.vo.StoryUuid;

final class StoryPersistenceMapper {

	private StoryPersistenceMapper() {
	}

	static Story toDomain(StoryJpaEntity entity) {
		List<StoryVisitCountry> countries = entity.visitCountries().stream()
				.map(StoryPersistenceMapper::toCountry)
				.toList();
		return Story.restore(
				entity.storyId(),
				StoryUuid.of(entity.storyUuid()),
				MemberUuid.of(entity.memberUuid()),
				entity.scheduleUuid(),
				entity.scheduleVisible(),
				entity.title(),
				entity.content(),
				entity.coverImageUrl(),
				entity.startDate(),
				entity.endDate(),
				entity.commentEnabled(),
				entity.visibilityScope(),
				entity.aiModerationStatus(),
				entity.viewCount(),
				entity.storyLikeCount(),
				entity.storyCommentCount(),
				entity.createdAt(),
				entity.updatedAt(),
				entity.deletedAt(),
				countries,
				flattenPlaces(countries),
				entity.tags().stream().map(tag -> StoryTag.restore(tag.storyTagId(), tag.tagName())).toList(),
				entity.visibilityMembers().stream()
						.map(member -> StoryVisibilityMember.restore(
								member.storyVisibilityMemberId(),
								MemberUuid.of(member.memberUuid()),
								member.createdAt()
						))
						.toList()
		);
	}

	static void apply(Story story, StoryJpaEntity entity) {
		if (entity.storyUuid() == null) {
			entity.assignIdentity(story.storyUuid().value(), story.memberUuid().value(), story.createdAt());
		}
		entity.apply(
				story.scheduleUuid(),
				story.scheduleVisible(),
				story.title(),
				story.content(),
				story.coverImageUrl(),
				story.startDate(),
				story.endDate(),
				story.commentEnabled(),
				story.visibilityScope(),
				story.aiModerationStatus(),
				story.viewCount(),
				story.storyLikeCount(),
				story.storyCommentCount(),
				story.updatedAt(),
				story.deletedAt()
		);
		entity.replaceVisitCountries(story.visitCountries().stream()
				.map(country -> toCountryEntity(entity, country))
				.toList());
		entity.replaceTags(story.tags().stream()
				.map(tag -> new StoryTagJpaEntity(entity, tag.tagName()))
				.toList());
		entity.replaceVisibilityMembers(story.visibilityMembers().stream()
				.map(member -> new StoryVisibilityMemberJpaEntity(
						entity,
						member.memberUuid().value(),
						member.createdAt()
				))
				.toList());
	}

	private static StoryVisitCountry toCountry(StoryVisitCountryJpaEntity entity) {
		return StoryVisitCountry.restore(
				entity.storyVisitCountryId(),
				entity.countryName(),
				entity.displayOrder(),
				entity.cities().stream()
						.map(city -> StoryVisitCity.restore(
								city.storyVisitCityId(),
								city.cityName(),
								city.displayOrder(),
								city.places().stream().map(StoryPersistenceMapper::toPlace).toList()
						))
						.toList()
		);
	}

	private static StoryPlace toPlace(StoryPlaceJpaEntity entity) {
		return StoryPlace.restore(
				entity.storyPlaceId(),
				entity.storyVisitCityId(),
				entity.placeName(),
				entity.displayOrder(),
				entity.images().stream()
						.map(image -> StoryPlaceImage.restore(
								image.storyPlaceImageId(),
								image.imageUrl(),
								image.imageOrder(),
								image.createdAt()
						))
						.toList()
		);
	}

	private static StoryVisitCountryJpaEntity toCountryEntity(StoryJpaEntity story, StoryVisitCountry country) {
		StoryVisitCountryJpaEntity entity = new StoryVisitCountryJpaEntity(
				story,
				country.countryName(),
				country.displayOrder()
		);
		entity.replaceCities(country.cities().stream()
				.map(city -> toCityEntity(story, entity, city))
				.toList());
		return entity;
	}

	private static StoryVisitCityJpaEntity toCityEntity(
			StoryJpaEntity story,
			StoryVisitCountryJpaEntity country,
			StoryVisitCity city
	) {
		StoryVisitCityJpaEntity entity = new StoryVisitCityJpaEntity(country, city.cityName(), city.displayOrder());
		entity.replacePlaces(city.places().stream()
				.map(place -> toPlaceEntity(story, entity, place))
				.toList());
		return entity;
	}

	private static StoryPlaceJpaEntity toPlaceEntity(
			StoryJpaEntity story,
			StoryVisitCityJpaEntity city,
			StoryPlace place
	) {
		StoryPlaceJpaEntity entity = new StoryPlaceJpaEntity(story, city, place.placeName(), place.displayOrder());
		entity.replaceImages(place.images().stream()
				.map(image -> new StoryPlaceImageJpaEntity(
						entity,
						image.imageUrl(),
						image.imageOrder(),
						image.createdAt()
				))
				.toList());
		return entity;
	}

	private static List<StoryPlace> flattenPlaces(List<StoryVisitCountry> countries) {
		return countries.stream()
				.flatMap(country -> country.cities().stream())
				.flatMap(city -> city.places().stream())
				.toList();
	}
}
