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
import com.planwith.planwith_fo_story.application.query.GetMyStoryDetailQuery;
import com.planwith.planwith_fo_story.application.query.GetMyStoryListQuery;
import com.planwith.planwith_fo_story.application.query.StoryDetailView;
import com.planwith.planwith_fo_story.domain.exception.StoryAccessDeniedException;
import com.planwith.planwith_fo_story.domain.exception.StoryNotFoundException;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;

@SpringBootTest
@ActiveProfiles("test")
class MyStoryQueryIntegrationTest {

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
	void listsOnlyOwnedActiveStoriesWithManagementFilters() {
		UUID ownerUuid = UUID.randomUUID();
		StoryDetailView japan = create(
				ownerUuid, "Japan", "일본", "도쿄", VisibilityScope.ALL,
				LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 10)
		);
		StoryDetailView korea = create(
				ownerUuid, "Korea", "한국", "서울", VisibilityScope.PRIVATE,
				LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 10)
		);
		create(
				UUID.randomUUID(), "Other", "일본", "도쿄", VisibilityScope.ALL,
				LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 10)
		);
		StoryDetailView deleted = create(
				ownerUuid, "Deleted", "일본", "오사카", VisibilityScope.ALL,
				LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 10)
		);
		storyCommandUseCase.delete(new DeleteStoryCommand(ownerUuid, UUID.fromString(deleted.storyUuid())));

		var japanResult = storyQueryUseCase.getMyStories(new GetMyStoryListQuery(
				ownerUuid,
				"일본",
				null,
				VisibilityScope.ALL,
				LocalDate.of(2026, 4, 5),
				LocalDate.of(2026, 4, 6),
				0,
				20
		));
		var koreaResult = storyQueryUseCase.getMyStories(new GetMyStoryListQuery(
				ownerUuid,
				null,
				"서울",
				VisibilityScope.PRIVATE,
				LocalDate.of(2026, 5, 10),
				LocalDate.of(2026, 5, 20),
				0,
				20
		));

		assertThat(japanResult.items()).extracting(item -> item.storyUuid()).containsExactly(japan.storyUuid());
		assertThat(koreaResult.items()).extracting(item -> item.storyUuid()).containsExactly(korea.storyUuid());
	}

	@Test
	void returnsOnlyOwnedActiveStoryDetail() {
		UUID ownerUuid = UUID.randomUUID();
		StoryDetailView owned = create(
				ownerUuid, "Owned", "일본", "도쿄", VisibilityScope.PRIVATE,
				LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 10)
		);
		UUID otherStoryUuid = UUID.fromString(create(
				UUID.randomUUID(), "Other", "한국", "서울", VisibilityScope.ALL,
				LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 10)
		).storyUuid());
		StoryDetailView deleted = create(
				ownerUuid, "Deleted", "한국", "부산", VisibilityScope.ALL,
				LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 10)
		);
		UUID deletedStoryUuid = UUID.fromString(deleted.storyUuid());
		storyCommandUseCase.delete(new DeleteStoryCommand(ownerUuid, deletedStoryUuid));

		var detail = storyQueryUseCase.getMyStoryDetail(new GetMyStoryDetailQuery(
				ownerUuid,
				UUID.fromString(owned.storyUuid())
		));

		assertThat(detail.storyUuid()).isEqualTo(owned.storyUuid());
		assertThatThrownBy(() -> storyQueryUseCase.getMyStoryDetail(
				new GetMyStoryDetailQuery(ownerUuid, otherStoryUuid)
		)).isInstanceOf(StoryAccessDeniedException.class);
		assertThatThrownBy(() -> storyQueryUseCase.getMyStoryDetail(
				new GetMyStoryDetailQuery(ownerUuid, deletedStoryUuid)
		)).isInstanceOf(StoryNotFoundException.class);
	}

	private StoryDetailView create(
			UUID authorUuid,
			String title,
			String country,
			String city,
			VisibilityScope visibilityScope,
			LocalDate startDate,
			LocalDate endDate
	) {
		return storyCommandUseCase.create(new CreateStoryCommand(
				authorUuid,
				null,
				false,
				title,
				"content",
				"https://img.example/cover.png",
				startDate,
				endDate,
				true,
				visibilityScope,
				false,
				List.of(new CreateStoryCommand.Country(
						country,
						0,
						List.of(new CreateStoryCommand.City(city, 0, List.of()))
				)),
				List.of(),
				visibilityScope == VisibilityScope.PRIVATE ? List.of(UUID.randomUUID()) : List.of()
		));
	}
}
