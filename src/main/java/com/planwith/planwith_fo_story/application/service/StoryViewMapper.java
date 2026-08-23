package com.planwith.planwith_fo_story.application.service;

import com.planwith.planwith_fo_story.application.query.StoryAuthorView;
import com.planwith.planwith_fo_story.application.query.StoryDetailView;
import com.planwith.planwith_fo_story.application.query.StoryPlaceImageView;
import com.planwith.planwith_fo_story.application.query.StoryPlaceView;
import com.planwith.planwith_fo_story.application.query.StorySummaryView;
import com.planwith.planwith_fo_story.application.query.StoryVisitCityView;
import com.planwith.planwith_fo_story.application.query.StoryVisitCountryView;
import com.planwith.planwith_fo_story.domain.model.Story;
import com.planwith.planwith_fo_story.domain.model.projection.MemberProfileProjection;

final class StoryViewMapper {

	private StoryViewMapper() {
	}

	static StoryDetailView toDetail(Story story, MemberProfileProjection authorProjection) {
		return new StoryDetailView(
				story.storyUuid().asString(),
				story.memberUuid().asString(),
				story.scheduleUuid() == null ? null : story.scheduleUuid().toString(),
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
				story.createdAt(),
				story.updatedAt(),
				story.visitCountries().stream()
						.map(country -> new StoryVisitCountryView(
								country.countryName(),
								country.displayOrder(),
								country.cities().stream()
										.map(city -> new StoryVisitCityView(city.cityName(), city.displayOrder()))
										.toList()
						))
						.toList(),
				story.places().stream()
						.map(place -> new StoryPlaceView(
								place.placeName(),
								place.displayOrder(),
								place.images().stream()
										.map(image -> new StoryPlaceImageView(image.imageUrl(), image.imageOrder()))
										.toList()
						))
						.toList(),
				story.tags().stream().map(tag -> tag.tagName()).toList(),
				story.visibilityMembers().stream().map(member -> member.memberUuid().asString()).toList(),
				toAuthor(story, authorProjection)
		);
	}

	static StorySummaryView toSummary(Story story, MemberProfileProjection authorProjection) {
		return new StorySummaryView(
				story.storyUuid().asString(),
				story.memberUuid().asString(),
				story.title(),
				story.coverImageUrl(),
				story.visitCountries().stream().map(country -> country.countryName()).toList(),
				story.visibilityScope(),
				story.viewCount(),
				story.storyLikeCount(),
				story.storyCommentCount(),
				story.createdAt(),
				toAuthor(story, authorProjection)
		);
	}

	private static StoryAuthorView toAuthor(Story story, MemberProfileProjection authorProjection) {
		if (authorProjection == null) {
			return new StoryAuthorView(story.memberUuid().asString(), null, null);
		}
		return new StoryAuthorView(
				authorProjection.memberUuid().asString(),
				authorProjection.nickname(),
				authorProjection.profileImage()
		);
	}
}
