package com.planwith.planwith_fo_story.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.planwith.planwith_fo_story.adapter.out.persistence.outbox.SpringDataStoryOutboxRepository;
import com.planwith.planwith_fo_story.adapter.out.persistence.story.SpringDataStoryRepository;
import com.planwith.planwith_fo_story.application.command.CreateStoryCommand;
import com.planwith.planwith_fo_story.application.command.CreateStoryCommandFactory;
import com.planwith.planwith_fo_story.application.port.in.StoryCommandUseCase;
import com.planwith.planwith_fo_story.application.port.out.StoryAiVerificationRequestPort;
import com.planwith.planwith_fo_story.application.query.StoryDetailView;
import com.planwith.planwith_fo_story.domain.exception.InvalidStoryStateException;
import com.planwith.planwith_fo_story.domain.model.AiModerationStatus;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;

@SpringBootTest
@ActiveProfiles("test")
class StoryCreateIntegrationTest {

	@Autowired
	private StoryCommandUseCase storyCommandUseCase;

	@Autowired
	private SpringDataStoryRepository storyRepository;

	@Autowired
	private SpringDataStoryOutboxRepository outboxRepository;

	@MockitoBean
	private StoryAiVerificationRequestPort storyAiVerificationRequestPort;

	@BeforeEach
	void setUp() {
		outboxRepository.deleteAll();
		storyRepository.deleteAll();
	}

	@Test
	void createPersistsStoryBodyAndChildren() {
		UUID memberUuid = UUID.randomUUID();
		UUID visibilityMemberUuid = UUID.randomUUID();

		StoryDetailView created = storyCommandUseCase.create(new CreateStoryCommand(
				memberUuid,
				null,
				false,
				"부산 여행",
				"해운대 기록을 남깁니다.",
				"https://img.example/cover.png",
				LocalDate.of(2026, 8, 1),
				LocalDate.of(2026, 8, 5),
				true,
				VisibilityScope.PRIVATE,
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
				List.of(visibilityMemberUuid)
		));

		assertThat(created.title()).isEqualTo("부산 여행");
		assertThat(created.aiModerationStatus()).isEqualTo(AiModerationStatus.UNVERIFIED);
		assertThat(created.visitCountries()).singleElement().satisfies(country -> {
			assertThat(country.countryName()).isEqualTo("Korea");
			assertThat(country.cities()).singleElement().satisfies(city -> {
				assertThat(city.cityName()).isEqualTo("Busan");
				assertThat(city.places()).singleElement().satisfies(place -> {
					assertThat(place.placeName()).isEqualTo("해운대");
					assertThat(place.images()).extracting("imageUrl").containsExactly("https://img.example/1.png");
				});
			});
		});
		assertThat(created.places()).extracting("placeName").containsExactly("해운대");
		assertThat(created.tags()).containsExactly("여행");
		assertThat(created.visibilityMemberUuids()).containsExactly(visibilityMemberUuid.toString());
		assertThat(storyRepository.count()).isEqualTo(1);
		assertThat(outboxRepository.count()).isEqualTo(1);
		verify(storyAiVerificationRequestPort, never()).requestVerification(any(), any());
	}

	@Test
	void createRequestsAiVerificationAfterCommit() {
		UUID memberUuid = UUID.randomUUID();
		CreateStoryCommand command = new CreateStoryCommand(
				memberUuid,
				null,
				false,
				"AI 검증 스토리",
				"본문입니다.",
				"https://img.example/cover.png",
				LocalDate.of(2026, 8, 1),
				LocalDate.of(2026, 8, 5),
				true,
				VisibilityScope.ALL,
				true,
				List.of(new CreateStoryCommand.Country(
						"Korea",
						0,
						List.of(new CreateStoryCommand.City("Seoul", 0, List.of()))
				)),
				List.of(),
				List.of()
		);

		StoryDetailView created = storyCommandUseCase.create(command);

		assertThat(created.aiModerationStatus()).isEqualTo(AiModerationStatus.UNVERIFIED);
		verify(storyAiVerificationRequestPort).requestVerification(
				UUID.fromString(created.storyUuid()),
				memberUuid
		);
	}

	@Test
	void createDoesNotRequestAiVerificationWhenValidationFails() {
		assertThatThrownBy(() -> storyCommandUseCase.create(CreateStoryCommandFactory.basic(
				UUID.randomUUID(),
				" ",
				"본문입니다.",
				"https://img.example/cover.png",
				VisibilityScope.ALL
		))).isInstanceOf(InvalidStoryStateException.class);

		assertThat(storyRepository.count()).isZero();
		verify(storyAiVerificationRequestPort, never()).requestVerification(any(), any());
	}
}
