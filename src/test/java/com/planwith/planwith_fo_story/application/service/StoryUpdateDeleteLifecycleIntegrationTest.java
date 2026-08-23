package com.planwith.planwith_fo_story.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.planwith.planwith_fo_story.adapter.out.persistence.outbox.SpringDataStoryOutboxRepository;
import com.planwith.planwith_fo_story.adapter.out.persistence.story.SpringDataStoryRepository;
import com.planwith.planwith_fo_story.application.command.CreateStoryCommand;
import com.planwith.planwith_fo_story.application.command.CreateStoryCommandFactory;
import com.planwith.planwith_fo_story.application.command.DeleteStoryCommand;
import com.planwith.planwith_fo_story.application.command.UpdateStoryCommand;
import com.planwith.planwith_fo_story.application.event.StoryDeletedEvent;
import com.planwith.planwith_fo_story.application.event.StoryUpdatedEvent;
import com.planwith.planwith_fo_story.application.port.in.StoryCommandUseCase;
import com.planwith.planwith_fo_story.application.port.in.StoryQueryUseCase;
import com.planwith.planwith_fo_story.application.port.out.StoryAiVerificationRequestPort;
import com.planwith.planwith_fo_story.application.port.out.StoryCommandPort;
import com.planwith.planwith_fo_story.application.port.out.StoryCounterPort;
import com.planwith.planwith_fo_story.application.query.GetStoryDetailQuery;
import com.planwith.planwith_fo_story.application.query.StoryDetailView;
import com.planwith.planwith_fo_story.domain.exception.StoryAccessDeniedException;
import com.planwith.planwith_fo_story.domain.exception.StoryNotFoundException;
import com.planwith.planwith_fo_story.domain.model.AiModerationStatus;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;

@SpringBootTest
@ActiveProfiles("test")
class StoryUpdateDeleteLifecycleIntegrationTest {

	@Autowired
	private StoryCommandUseCase storyCommandUseCase;

	@Autowired
	private StoryQueryUseCase storyQueryUseCase;

	@Autowired
	private StoryCommandPort storyCommandPort;

	@Autowired
	private StoryCounterPort storyCounterPort;

	@Autowired
	private SpringDataStoryRepository storyRepository;

	@Autowired
	private SpringDataStoryOutboxRepository outboxRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@MockitoBean
	private StoryAiVerificationRequestPort storyAiVerificationRequestPort;

	@BeforeEach
	void setUp() {
		outboxRepository.deleteAll();
		storyRepository.deleteAll();
	}

	@Test
	void updatesAllEditableFieldsResetsAiAndPreservesIdentityAndCounters() {
		UUID authorUuid = UUID.randomUUID();
		UUID visibilityMemberUuid = UUID.randomUUID();
		StoryDetailView created = storyCommandUseCase.create(CreateStoryCommandFactory.basic(
				authorUuid, "Original", "Original content", "https://img.example/original.png", VisibilityScope.ALL
		));
		UUID storyUuid = UUID.fromString(created.storyUuid());
		storyCommandPort.updateAiModerationStatus(storyUuid, AiModerationStatus.VERIFIED, LocalDateTime.now())
				.orElseThrow();
		storyCounterPort.incrementViewCount(storyUuid);
		storyCounterPort.changeLikeCount(storyUuid, 2L);
		storyCounterPort.changeCommentCount(storyUuid, 3L);

		StoryDetailView updated = storyCommandUseCase.update(new UpdateStoryCommand(
				authorUuid,
				storyUuid,
				null,
				false,
				"Updated",
				"Updated content",
				"https://img.example/updated.png",
				LocalDate.of(2026, 9, 1),
				LocalDate.of(2026, 9, 10),
				false,
				VisibilityScope.PRIVATE,
				true,
				List.of(new CreateStoryCommand.Country(
						"France",
						0,
						List.of(new CreateStoryCommand.City(
								"Paris",
								0,
								List.of(new CreateStoryCommand.Place(
										"Eiffel Tower",
										0,
										List.of(new CreateStoryCommand.PlaceImage(
												"https://img.example/eiffel.png",
												1
										))
								))
						))
				)),
				List.of("france", "paris"),
				List.of(visibilityMemberUuid)
		));

		assertThat(updated.storyUuid()).isEqualTo(created.storyUuid());
		assertThat(updated.memberUuid()).isEqualTo(created.memberUuid());
		assertThat(updated.createdAt()).isCloseTo(created.createdAt(), within(2, ChronoUnit.MICROS));
		assertThat(updated.viewCount()).isEqualTo(1L);
		assertThat(updated.storyLikeCount()).isEqualTo(2L);
		assertThat(updated.storyCommentCount()).isEqualTo(3L);
		assertThat(updated.aiModerationStatus()).isEqualTo(AiModerationStatus.UNVERIFIED);
		assertThat(updated.commentEnabled()).isFalse();
		assertThat(updated.visibilityScope()).isEqualTo(VisibilityScope.PRIVATE);
		assertThat(updated.visitCountries()).singleElement().satisfies(country -> {
			assertThat(country.countryName()).isEqualTo("France");
			assertThat(country.cities()).singleElement().satisfies(city -> {
				assertThat(city.cityName()).isEqualTo("Paris");
				assertThat(city.places()).singleElement().satisfies(place -> {
					assertThat(place.placeName()).isEqualTo("Eiffel Tower");
					assertThat(place.images()).singleElement()
							.satisfies(image -> assertThat(image.imageUrl()).endsWith("eiffel.png"));
				});
			});
		});
		assertThat(updated.tags()).containsExactly("france", "paris");
		assertThat(updated.visibilityMemberUuids()).containsExactly(visibilityMemberUuid.toString());
		assertThat(outboxRepository.findAll()).anySatisfy(outbox ->
				assertThat(outbox.eventType()).isEqualTo(StoryUpdatedEvent.EVENT_TYPE));
		verify(storyAiVerificationRequestPort).requestVerification(storyUuid, authorUuid);
	}

	@Test
	void softDeletesStoryKeepsChildrenAndAppendsDeletedEvent() {
		UUID authorUuid = UUID.randomUUID();
		StoryDetailView created = storyCommandUseCase.create(CreateStoryCommandFactory.basic(
				authorUuid, "Delete", "content", "https://img.example/cover.png", VisibilityScope.ALL
		));
		UUID storyUuid = UUID.fromString(created.storyUuid());
		Long countryIdBefore = jdbcTemplate.queryForObject(
				"select min(story_visit_country_id) from story_visit_country",
				Long.class
		);
		Long cityIdBefore = jdbcTemplate.queryForObject(
				"select min(story_visit_city_id) from story_visit_city",
				Long.class
		);

		storyCommandUseCase.delete(new DeleteStoryCommand(authorUuid, storyUuid));

		var deleted = storyCommandPort.findByStoryUuid(storyUuid).orElseThrow();
		assertThat(deleted.isDeleted()).isTrue();
		assertThat(deleted.visitCountries()).isNotEmpty();
		assertThat(deleted.visitCountries().get(0).cities()).isNotEmpty();
		assertThat(storyRepository.count()).isEqualTo(1L);
		assertThat(jdbcTemplate.queryForObject(
				"select min(story_visit_country_id) from story_visit_country",
				Long.class
		)).isEqualTo(countryIdBefore);
		assertThat(jdbcTemplate.queryForObject(
				"select min(story_visit_city_id) from story_visit_city",
				Long.class
		)).isEqualTo(cityIdBefore);
		assertThat(outboxRepository.findAll()).anySatisfy(outbox -> {
			assertThat(outbox.eventType()).isEqualTo(StoryDeletedEvent.EVENT_TYPE);
			assertThat(outbox.payload()).contains(storyUuid.toString());
		});
		assertThatThrownBy(() -> storyQueryUseCase.getDetail(new GetStoryDetailQuery(storyUuid, authorUuid)))
				.isInstanceOf(StoryNotFoundException.class);
	}

	@Test
	void rejectsUpdateAndDeleteFromAnotherMember() {
		UUID authorUuid = UUID.randomUUID();
		UUID otherUuid = UUID.randomUUID();
		StoryDetailView created = storyCommandUseCase.create(CreateStoryCommandFactory.basic(
				authorUuid, "Owned", "content", "https://img.example/cover.png", VisibilityScope.ALL
		));
		UUID storyUuid = UUID.fromString(created.storyUuid());

		assertThatThrownBy(() -> storyCommandUseCase.update(coreUpdate(otherUuid, created)))
				.isInstanceOf(StoryAccessDeniedException.class);
		assertThatThrownBy(() -> storyCommandUseCase.delete(new DeleteStoryCommand(otherUuid, storyUuid)))
				.isInstanceOf(StoryAccessDeniedException.class);
		assertThat(storyCommandPort.findByStoryUuid(storyUuid)).get().satisfies(story ->
				assertThat(story.isDeleted()).isFalse());
	}

	private static UpdateStoryCommand coreUpdate(UUID actorUuid, StoryDetailView story) {
		return new UpdateStoryCommand(
				actorUuid,
				UUID.fromString(story.storyUuid()),
				null,
				false,
				"Changed",
				story.content(),
				story.coverImageUrl(),
				story.startDate(),
				story.endDate()
		);
	}
}
