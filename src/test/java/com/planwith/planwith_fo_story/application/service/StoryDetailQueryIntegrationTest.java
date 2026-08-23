package com.planwith.planwith_fo_story.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.planwith.planwith_fo_story.adapter.out.persistence.outbox.SpringDataStoryOutboxRepository;
import com.planwith.planwith_fo_story.adapter.out.persistence.story.SpringDataStoryRepository;
import com.planwith.planwith_fo_story.application.command.CreateStoryCommand;
import com.planwith.planwith_fo_story.application.command.DeleteStoryCommand;
import com.planwith.planwith_fo_story.application.port.in.StoryCommandUseCase;
import com.planwith.planwith_fo_story.application.port.in.StoryQueryUseCase;
import com.planwith.planwith_fo_story.application.query.GetStoryDetailQuery;
import com.planwith.planwith_fo_story.application.query.StoryDetailView;
import com.planwith.planwith_fo_story.domain.exception.StoryNotFoundException;
import com.planwith.planwith_fo_story.domain.model.AiModerationStatus;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;

@SpringBootTest
@ActiveProfiles("test")
class StoryDetailQueryIntegrationTest {

	@Autowired
	private StoryCommandUseCase storyCommandUseCase;

	@Autowired
	private StoryQueryUseCase storyQueryUseCase;

	@Autowired
	private SpringDataStoryRepository storyRepository;

	@Autowired
	private SpringDataStoryOutboxRepository outboxRepository;

	@BeforeEach
	void setUp() {
		outboxRepository.deleteAll();
		storyRepository.deleteAll();
	}

	@Test
	void getDetailReturnsStoryOwnFieldsWithoutIncrementingViewCount() {
		UUID authorUuid = UUID.randomUUID();
		StoryDetailView created = storyCommandUseCase.create(new CreateStoryCommand(
				authorUuid,
				null,
				false,
				"부산 여행",
				"해운대 기록을 남깁니다.",
				"https://img.example/cover.png",
				LocalDate.of(2026, 8, 1),
				LocalDate.of(2026, 8, 5),
				true,
				VisibilityScope.ALL,
				false,
				List.of(new CreateStoryCommand.Country(
						"Korea",
						0,
						List.of(new CreateStoryCommand.City(
								"Busan",
								0,
								List.of(new CreateStoryCommand.Place(
										"해운대",
										0,
										List.of(new CreateStoryCommand.PlaceImage("https://img.example/1.png", 1))
								))
						))
				)),
				List.of("여행"),
				List.of()
		));

		StoryDetailView first = storyQueryUseCase.getDetail(new GetStoryDetailQuery(
				UUID.fromString(created.storyUuid()),
				null
		));
		StoryDetailView second = storyQueryUseCase.getDetail(new GetStoryDetailQuery(
				UUID.fromString(created.storyUuid()),
				null
		));

		assertThat(first.storyUuid()).isEqualTo(created.storyUuid());
		assertThat(first.memberUuid()).isEqualTo(authorUuid.toString());
		assertThat(first.title()).isEqualTo("부산 여행");
		assertThat(first.content()).isEqualTo("해운대 기록을 남깁니다.");
		assertThat(first.coverImageUrl()).isEqualTo("https://img.example/cover.png");
		assertThat(first.startDate()).isEqualTo(LocalDate.of(2026, 8, 1));
		assertThat(first.endDate()).isEqualTo(LocalDate.of(2026, 8, 5));
		assertThat(first.commentEnabled()).isTrue();
		assertThat(first.visibilityScope()).isEqualTo(VisibilityScope.ALL);
		assertThat(first.aiModerationStatus()).isEqualTo(AiModerationStatus.UNVERIFIED);
		assertThat(first.tags()).containsExactly("여행");
		assertThat(first.visitCountries()).singleElement().satisfies(country -> {
			assertThat(country.countryName()).isEqualTo("Korea");
			assertThat(country.cities()).singleElement().satisfies(city -> {
				assertThat(city.cityName()).isEqualTo("Busan");
				assertThat(city.places()).singleElement().satisfies(place -> {
					assertThat(place.placeName()).isEqualTo("해운대");
					assertThat(place.images()).extracting("imageUrl").containsExactly("https://img.example/1.png");
				});
			});
		});
		assertThat(first.places()).extracting("placeName").containsExactly("해운대");
		assertThat(first.viewCount()).isZero();
		assertThat(second.viewCount()).isEqualTo(first.viewCount());
	}

	@Test
	void deletedStoryIsNotFoundOnDetail() {
		UUID authorUuid = UUID.randomUUID();
		StoryDetailView created = storyCommandUseCase.create(new CreateStoryCommand(
				authorUuid,
				null,
				false,
				"삭제될 스토리",
				"본문입니다.",
				"https://img.example/cover.png",
				LocalDate.of(2026, 8, 1),
				LocalDate.of(2026, 8, 5),
				true,
				VisibilityScope.ALL,
				false,
				List.of(new CreateStoryCommand.Country(
						"Korea",
						0,
						List.of(new CreateStoryCommand.City("Seoul", 0, List.of()))
				)),
				List.of(),
				List.of()
		));
		storyCommandUseCase.delete(new DeleteStoryCommand(authorUuid, UUID.fromString(created.storyUuid())));

		assertThatThrownBy(() -> storyQueryUseCase.getDetail(new GetStoryDetailQuery(
				UUID.fromString(created.storyUuid()),
				authorUuid
		))).isInstanceOf(StoryNotFoundException.class);
	}
}
