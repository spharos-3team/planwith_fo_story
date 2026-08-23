package com.planwith.planwith_fo_story.adapter.out.persistence.story;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_story.application.port.out.StoryCommandPort;
import com.planwith.planwith_fo_story.application.port.out.StoryQueryPort;
import com.planwith.planwith_fo_story.domain.model.AiModerationStatus;
import com.planwith.planwith_fo_story.domain.model.Story;
import com.planwith.planwith_fo_story.domain.model.StoryPlace;
import com.planwith.planwith_fo_story.domain.model.StoryPlaceImage;
import com.planwith.planwith_fo_story.domain.model.StoryTag;
import com.planwith.planwith_fo_story.domain.model.StoryVisitCity;
import com.planwith.planwith_fo_story.domain.model.StoryVisitCountry;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;
import com.planwith.planwith_fo_story.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_story.domain.model.vo.StoryUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StoryPersistenceAdapterIntegrationTest {

	@Autowired
	private StoryCommandPort storyCommandPort;

	@Autowired
	private StoryQueryPort storyQueryPort;

	@Test
	void savesAndLoadsStoryAggregateMatchingDdl() {
		UUID memberUuid = UUID.randomUUID();
		LocalDateTime now = LocalDateTime.of(2026, 8, 23, 10, 0);
		Story created = Story.create(
				StoryUuid.generate(),
				MemberUuid.of(memberUuid),
				null,
				false,
				"여행 기록",
				"내용을 작성합니다.",
				"https://img.example/cover.png",
				LocalDate.of(2026, 8, 1),
				LocalDate.of(2026, 8, 5),
				true,
				VisibilityScope.MEMBER,
				now
		).replaceChildren(
				List.of(StoryVisitCountry.create(
						"Korea",
						0,
						List.of(StoryVisitCity.create(
								"Busan",
								0,
								List.of(StoryPlace.create(null, "해운대", 0, List.of(
										StoryPlaceImage.create("https://img.example/1.png", 1, now)
								)))
						))
				)),
				List.of(StoryTag.create("여행")),
				List.of(),
				now
		);

		Story saved = storyCommandPort.save(created);
		Story loaded = storyQueryPort.findActiveByStoryUuid(saved.storyUuid().value()).orElseThrow();

		assertThat(loaded.title()).isEqualTo("여행 기록");
		assertThat(loaded.scheduleVisible()).isFalse();
		assertThat(loaded.viewCount()).isZero();
		assertThat(loaded.storyCommentCount()).isZero();
		assertThat(loaded.updatedAt()).isNotNull();
		assertThat(loaded.visibilityScope()).isEqualTo(VisibilityScope.MEMBER);
		assertThat(loaded.aiModerationStatus()).isEqualTo(AiModerationStatus.UNVERIFIED);
		assertThat(loaded.visitCountries()).extracting(StoryVisitCountry::countryName).containsExactly("Korea");
		assertThat(loaded.visitCountries().get(0).cities()).singleElement().satisfies(city -> {
			assertThat(city.cityName()).isEqualTo("Busan");
			assertThat(city.places()).extracting(StoryPlace::placeName).containsExactly("해운대");
			assertThat(city.places().get(0).images()).extracting(StoryPlaceImage::imageUrl)
					.containsExactly("https://img.example/1.png");
		});
		assertThat(loaded.places()).extracting(StoryPlace::placeName).containsExactly("해운대");
		assertThat(loaded.tags()).extracting(StoryTag::tagName).containsExactly("여행");
		assertThat(loaded.deletedAt()).isNull();
		assertThat(storyQueryPort.findActiveByMemberUuid(memberUuid, 0, 10)).hasSize(1);
	}
}
