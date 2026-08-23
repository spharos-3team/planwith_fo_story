package com.planwith.planwith_fo_story.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_story.domain.exception.InvalidStoryStateException;
import com.planwith.planwith_fo_story.domain.model.vo.MemberUuid;

class StoryDomainModelTest {

	private final MemberUuid author = MemberUuid.of(UUID.randomUUID());

	@Test
	void createAppliesDefaultCountersAndUnverifiedStatus() {
		Story story = StoryTestFactory.create(author, VisibilityScope.ALL);

		assertThat(story.scheduleVisible()).isFalse();
		assertThat(story.coverImageUrl()).isEqualTo("https://img.example/cover.png");
		assertThat(story.startDate()).isEqualTo(LocalDate.of(2026, 8, 1));
		assertThat(story.endDate()).isEqualTo(LocalDate.of(2026, 8, 5));
		assertThat(story.aiModerationStatus()).isEqualTo(AiModerationStatus.UNVERIFIED);
		assertThat(story.viewCount()).isZero();
		assertThat(story.storyLikeCount()).isZero();
		assertThat(story.storyCommentCount()).isZero();
		assertThat(story.visitCountries()).isEmpty();
		assertThat(story.places()).isEmpty();
		assertThat(story.tags()).isEmpty();
		assertThat(story.visibilityMembers()).isEmpty();
	}

	@Test
	void applyAiModerationResultMarksVerified() {
		Story verified = StoryTestFactory.create(author, VisibilityScope.ALL)
				.applyAiModerationResult(AiModerationStatus.VERIFIED, LocalDateTime.of(2026, 8, 23, 21, 0));

		assertThat(verified.aiModerationStatus()).isEqualTo(AiModerationStatus.VERIFIED);
	}

	@Test
	void replaceChildrenKeepsStoryAggregateHierarchy() {
		LocalDateTime now = LocalDateTime.of(2026, 8, 23, 12, 0);
		Story story = StoryTestFactory.create(author, VisibilityScope.PRIVATE)
				.replaceChildren(
						List.of(StoryVisitCountry.create(
								"Korea",
								0,
								List.of(StoryVisitCity.create(
										"Busan",
										0,
										List.of(StoryPlace.create(
												null,
												"해운대",
												0,
												List.of(StoryPlaceImage.create("https://img.example/1.png", 1, now))
										))
								))
						)),
						List.of(StoryTag.create("여행")),
						List.of(StoryVisibilityMember.create(MemberUuid.of(UUID.randomUUID()), now)),
						now
				);

		assertThat(story.visitCountries()).singleElement().satisfies(country -> {
			assertThat(country.countryName()).isEqualTo("Korea");
			assertThat(country.cities()).singleElement().satisfies(city -> {
				assertThat(city.cityName()).isEqualTo("Busan");
				assertThat(city.places()).extracting(StoryPlace::placeName).containsExactly("해운대");
			});
		});
		assertThat(story.places()).extracting(StoryPlace::placeName).containsExactly("해운대");
		assertThat(story.places().get(0).images()).extracting(StoryPlaceImage::imageOrder).containsExactly(1);
		assertThat(story.tags()).extracting(StoryTag::tagName).containsExactly("여행");
		assertThat(story.visibilityMembers()).hasSize(1);
	}

	@Test
	void rejectsInvalidPlaceImageOrder() {
		assertThatThrownBy(() -> StoryPlaceImage.create("https://img.example/1.png", 6, LocalDateTime.now()))
				.isInstanceOf(InvalidStoryStateException.class)
				.hasMessageContaining("1부터 5까지");
	}

	@Test
	void requiresCoverImageAndTravelPeriod() {
		assertThatThrownBy(() -> Story.create(
				com.planwith.planwith_fo_story.domain.model.vo.StoryUuid.generate(),
				author,
				null,
				false,
				"제목",
				"본문",
				" ",
				LocalDate.of(2026, 8, 1),
				LocalDate.of(2026, 8, 5),
				true,
				VisibilityScope.ALL,
				LocalDateTime.of(2026, 8, 23, 11, 0)
		)).isInstanceOf(InvalidStoryStateException.class)
				.hasMessageContaining("커버 이미지");
	}

	@Test
	void rejectsScheduleVisibleWithoutScheduleUuid() {
		assertThatThrownBy(() -> Story.create(
				com.planwith.planwith_fo_story.domain.model.vo.StoryUuid.generate(),
				author,
				null,
				true,
				"제목",
				"본문",
				"https://img.example/cover.png",
				LocalDate.of(2026, 8, 1),
				LocalDate.of(2026, 8, 5),
				true,
				VisibilityScope.ALL,
				LocalDateTime.of(2026, 8, 23, 11, 0)
		)).isInstanceOf(InvalidStoryStateException.class)
				.hasMessageContaining("일정 UUID");
	}

	@Test
	void rejectsVideoCoverImage() {
		assertThatThrownBy(() -> Story.create(
				com.planwith.planwith_fo_story.domain.model.vo.StoryUuid.generate(),
				author,
				null,
				false,
				"제목",
				"본문",
				"https://img.example/cover.mp4",
				LocalDate.of(2026, 8, 1),
				LocalDate.of(2026, 8, 5),
				true,
				VisibilityScope.ALL,
				LocalDateTime.of(2026, 8, 23, 11, 0)
		)).isInstanceOf(InvalidStoryStateException.class)
				.hasMessageContaining("동영상");
	}

	@Test
	void rejectsInvalidTravelPeriod() {
		assertThatThrownBy(() -> Story.create(
				com.planwith.planwith_fo_story.domain.model.vo.StoryUuid.generate(),
				author,
				null,
				false,
				"제목",
				"본문",
				"https://img.example/cover.png",
				LocalDate.of(2026, 8, 5),
				LocalDate.of(2026, 8, 1),
				true,
				VisibilityScope.ALL,
				LocalDateTime.of(2026, 8, 23, 11, 0)
		)).isInstanceOf(InvalidStoryStateException.class)
				.hasMessageContaining("종료일");
	}

	@Test
	void rejectsDuplicateCountryOnReplaceChildren() {
		LocalDateTime now = LocalDateTime.of(2026, 8, 23, 12, 0);
		Story story = StoryTestFactory.create(author, VisibilityScope.ALL);

		assertThatThrownBy(() -> story.replaceChildren(
				List.of(
						StoryVisitCountry.create("Korea", 0, List.of(StoryVisitCity.create("Seoul", 0))),
						StoryVisitCountry.create("korea", 1, List.of(StoryVisitCity.create("Busan", 0)))
				),
				List.of(),
				List.of(),
				now
		)).isInstanceOf(InvalidStoryStateException.class)
				.hasMessageContaining("방문국가");
	}

	@Test
	void keepsCountryCityPlaceImageHierarchy() {
		LocalDateTime now = LocalDateTime.of(2026, 8, 23, 12, 0);
		Story story = StoryTestFactory.create(author, VisibilityScope.ALL)
				.replaceChildren(
						List.of(StoryVisitCountry.create(
								"일본",
								0,
								List.of(
										StoryVisitCity.create(
												"도쿄",
												0,
												List.of(
														StoryPlace.create(
																null,
																"시부야",
																0,
																List.of(
																		StoryPlaceImage.create("https://img.example/1.png", 1, now),
																		StoryPlaceImage.create("https://img.example/2.png", 2, now),
																		StoryPlaceImage.create("https://img.example/3.png", 3, now)
																)
														),
														StoryPlace.create(null, "도쿄타워", 1, List.of())
												)
										),
										StoryVisitCity.create(
												"오사카",
												1,
												List.of(StoryPlace.create(null, "도톤보리", 0, List.of()))
										)
								)
						)),
						List.of(StoryTag.create("여행"), StoryTag.create("일본")),
						List.of(),
						now
				);

		assertThat(story.visitCountries().get(0).cities()).extracting(StoryVisitCity::cityName)
				.containsExactly("도쿄", "오사카");
		assertThat(story.visitCountries().get(0).cities().get(0).places())
				.extracting(StoryPlace::placeName)
				.containsExactly("시부야", "도쿄타워");
		assertThat(story.visitCountries().get(0).cities().get(0).places().get(0).images())
				.extracting(StoryPlaceImage::imageOrder)
				.containsExactly(1, 2, 3);
		assertThat(story.places()).extracting(StoryPlace::placeName)
				.containsExactly("시부야", "도쿄타워", "도톤보리");
		assertThat(story.tags()).extracting(StoryTag::tagName).containsExactly("여행", "일본");
	}

	@Test
	void rejectsMoreThanFivePlaceImages() {
		LocalDateTime now = LocalDateTime.of(2026, 8, 23, 12, 0);
		assertThatThrownBy(() -> StoryPlace.create(
				null,
				"시부야",
				0,
				List.of(
						StoryPlaceImage.create("https://img.example/1.png", 1, now),
						StoryPlaceImage.create("https://img.example/2.png", 2, now),
						StoryPlaceImage.create("https://img.example/3.png", 3, now),
						StoryPlaceImage.create("https://img.example/4.png", 4, now),
						StoryPlaceImage.create("https://img.example/5.png", 5, now),
						StoryPlaceImage.create("https://img.example/6.png", 1, now)
				)
		)).isInstanceOf(InvalidStoryStateException.class)
				.hasMessageContaining("최대 5개");
	}
}
