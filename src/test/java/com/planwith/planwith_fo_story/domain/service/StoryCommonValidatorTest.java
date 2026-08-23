package com.planwith.planwith_fo_story.domain.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_story.domain.exception.InvalidStoryStateException;
import com.planwith.planwith_fo_story.domain.exception.MemberAuthenticationRequiredException;
import com.planwith.planwith_fo_story.domain.model.StoryPlace;
import com.planwith.planwith_fo_story.domain.model.StoryPlaceImage;
import com.planwith.planwith_fo_story.domain.model.StoryTag;
import com.planwith.planwith_fo_story.domain.model.StoryVisibilityMember;
import com.planwith.planwith_fo_story.domain.model.StoryVisitCity;
import com.planwith.planwith_fo_story.domain.model.StoryVisitCountry;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;
import com.planwith.planwith_fo_story.domain.model.vo.MemberUuid;

class StoryCommonValidatorTest {

	private final StoryCommonValidator validator = new StoryCommonValidator();
	private final LocalDateTime now = LocalDateTime.of(2026, 8, 23, 12, 0);

	@Test
	void validateWriteAcceptsCompletePayload() {
		assertThatCode(() -> validator.validateWrite(validSpec())).doesNotThrowAnyException();
	}

	@Test
	void rejectsNullAuthor() {
		assertThatThrownBy(() -> validator.validateAuthor(null))
				.isInstanceOf(MemberAuthenticationRequiredException.class);
	}

	@Test
	void rejectsScheduleVisibleWithoutScheduleUuid() {
		assertThatThrownBy(() -> validator.validateScheduleShare(null, true))
				.isInstanceOf(InvalidStoryStateException.class)
				.hasMessageContaining("일정 UUID");
	}

	@Test
	void allowsScheduleUuidWithoutSharing() {
		assertThatCode(() -> validator.validateScheduleShare(UUID.randomUUID(), false))
				.doesNotThrowAnyException();
	}

	@Test
	void rejectsMissingVisitCountry() {
		StoryWriteSpec spec = specWith(
				List.of(),
				List.of(),
				List.of(),
				List.of()
		);

		assertThatThrownBy(() -> validator.validateWrite(spec))
				.isInstanceOf(InvalidStoryStateException.class)
				.hasMessageContaining("방문국가");
	}

	@Test
	void rejectsCountryWithoutCity() {
		StoryWriteSpec spec = specWith(
				List.of(StoryVisitCountry.create("Korea", 0, List.of())),
				List.of(),
				List.of(),
				List.of()
		);

		assertThatThrownBy(() -> validator.validateWrite(spec))
				.isInstanceOf(InvalidStoryStateException.class)
				.hasMessageContaining("방문도시");
	}

	@Test
	void rejectsDuplicateCityInSameCountry() {
		StoryWriteSpec spec = specWith(
				List.of(StoryVisitCountry.create(
						"Korea",
						0,
						List.of(StoryVisitCity.create("Seoul", 0), StoryVisitCity.create("seoul", 1))
				)),
				List.of(),
				List.of(),
				List.of()
		);

		assertThatThrownBy(() -> validator.validateWrite(spec))
				.isInstanceOf(InvalidStoryStateException.class)
				.hasMessageContaining("방문도시");
	}

	@Test
	void rejectsDuplicateTag() {
		StoryWriteSpec spec = specWith(
				List.of(StoryVisitCountry.create("Korea", 0, List.of(StoryVisitCity.create("Seoul", 0)))),
				List.of(),
				List.of(StoryTag.create("여행"), StoryTag.create("여행")),
				List.of()
		);

		assertThatThrownBy(() -> validator.validateWrite(spec))
				.isInstanceOf(InvalidStoryStateException.class)
				.hasMessageContaining("태그");
	}

	@Test
	void rejectsPrivateStoryWithoutVisibilityMembers() {
		assertThatThrownBy(() -> validator.validateVisibilityMembers(VisibilityScope.PRIVATE, List.of()))
				.isInstanceOf(InvalidStoryStateException.class)
				.hasMessageContaining("지정 회원");
	}

	@Test
	void rejectsVisibilityMembersWhenNotPrivate() {
		assertThatThrownBy(() -> validator.validateVisibilityMembers(
				VisibilityScope.ALL,
				List.of(StoryVisibilityMember.create(MemberUuid.of(UUID.randomUUID()), now))
		)).isInstanceOf(InvalidStoryStateException.class)
				.hasMessageContaining("비공개");
	}

	@Test
	void rejectsVideoUrl() {
		assertThatThrownBy(() -> validator.rejectVideoUrl("https://cdn.example/clip.MP4", "대표사진"))
				.isInstanceOf(InvalidStoryStateException.class)
				.hasMessageContaining("동영상");
	}

	@Test
	void rejectsEndDateBeforeStartDate() {
		assertThatThrownBy(() -> validator.validateBody(
				null,
				false,
				"https://img.example/cover.png",
				LocalDate.of(2026, 8, 5),
				LocalDate.of(2026, 8, 1),
				VisibilityScope.ALL
		)).isInstanceOf(InvalidStoryStateException.class)
				.hasMessageContaining("종료일");
	}

	@Test
	void placesAreOptional() {
		StoryWriteSpec spec = specWith(
				List.of(StoryVisitCountry.create("Korea", 0, List.of(StoryVisitCity.create("Seoul", 0)))),
				List.of(),
				List.of(),
				List.of()
		);

		assertThatCode(() -> validator.validateWrite(spec)).doesNotThrowAnyException();
	}

	private StoryWriteSpec validSpec() {
		return specWith(
				List.of(StoryVisitCountry.create("Korea", 0, List.of(StoryVisitCity.create("Seoul", 0)))),
				List.of(StoryPlace.create(
						null,
						"광화문",
						0,
						List.of(StoryPlaceImage.create("https://img.example/1.png", 1, now))
				)),
				List.of(StoryTag.create("여행")),
				List.of()
		);
	}

	private StoryWriteSpec specWith(
			List<StoryVisitCountry> countries,
			List<StoryPlace> places,
			List<StoryTag> tags,
			List<StoryVisibilityMember> members
	) {
		return new StoryWriteSpec(
				null,
				false,
				"제목",
				"본문",
				"https://img.example/cover.png",
				LocalDate.of(2026, 8, 1),
				LocalDate.of(2026, 8, 5),
				VisibilityScope.ALL,
				countries,
				places,
				tags,
				members
		);
	}
}
