package com.planwith.planwith_fo_story.domain.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.planwith.planwith_fo_story.domain.model.StoryPlace;
import com.planwith.planwith_fo_story.domain.model.StoryTag;
import com.planwith.planwith_fo_story.domain.model.StoryVisibilityMember;
import com.planwith.planwith_fo_story.domain.model.StoryVisitCountry;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;

public record StoryWriteSpec(
		UUID scheduleUuid,
		boolean scheduleVisible,
		String title,
		String content,
		String coverImageUrl,
		LocalDate startDate,
		LocalDate endDate,
		VisibilityScope visibilityScope,
		List<StoryVisitCountry> visitCountries,
		List<StoryPlace> places,
		List<StoryTag> tags,
		List<StoryVisibilityMember> visibilityMembers
) {
}
