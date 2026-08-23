package com.planwith.planwith_fo_story.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
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
import com.planwith.planwith_fo_story.application.command.DeleteStoryCommand;
import com.planwith.planwith_fo_story.application.port.in.StoryCommandUseCase;
import com.planwith.planwith_fo_story.application.port.in.StoryQueryUseCase;
import com.planwith.planwith_fo_story.application.port.out.StoryNicknameSearchPort;
import com.planwith.planwith_fo_story.application.query.SearchStoryQuery;
import com.planwith.planwith_fo_story.application.query.StoryDetailView;
import com.planwith.planwith_fo_story.application.query.StorySearchType;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;

@SpringBootTest
@ActiveProfiles("test")
class StorySearchIntegrationTest {

	@Autowired
	private StoryCommandUseCase storyCommandUseCase;

	@Autowired
	private StoryQueryUseCase storyQueryUseCase;

	@Autowired
	private SpringDataStoryRepository storyRepository;

	@Autowired
	private SpringDataStoryOutboxRepository outboxRepository;

	@MockitoBean
	private StoryNicknameSearchPort storyNicknameSearchPort;

	@BeforeEach
	void setUp() {
		outboxRepository.deleteAll();
		storyRepository.deleteAll();
	}

	@Test
	void searchesCountryAndCityAndExcludesDeletedOrUnreadableStories() {
		UUID publicAuthor = UUID.randomUUID();
		StoryDetailView publicStory = create(publicAuthor, "public", "일본", "도쿄", VisibilityScope.ALL);
		create(UUID.randomUUID(), "member", "일본", "도쿄", VisibilityScope.MEMBER);
		StoryDetailView deletedStory = create(UUID.randomUUID(), "deleted", "일본", "오사카", VisibilityScope.ALL);
		storyCommandUseCase.delete(new DeleteStoryCommand(
				UUID.fromString(deletedStory.memberUuid()),
				UUID.fromString(deletedStory.storyUuid())
		));

		var byCountry = storyQueryUseCase.search(new SearchStoryQuery(
				StorySearchType.COUNTRY, "일", null, 0, 20
		));
		var byCity = storyQueryUseCase.search(new SearchStoryQuery(
				StorySearchType.CITY, "도쿄", null, 0, 20
		));

		assertThat(byCountry.items()).extracting(item -> item.storyUuid()).containsExactly(publicStory.storyUuid());
		assertThat(byCity.items()).extracting(item -> item.storyUuid()).containsExactly(publicStory.storyUuid());
	}

	@Test
	void searchesNicknameThroughMemberUuidsAndAppliesVisibilityPolicyLast() {
		UUID matchedAuthor = UUID.randomUUID();
		StoryDetailView visible = create(matchedAuthor, "visible", "한국", "서울", VisibilityScope.ALL);
		create(matchedAuthor, "member-only", "한국", "부산", VisibilityScope.MEMBER);
		create(UUID.randomUUID(), "other", "한국", "서울", VisibilityScope.ALL);
		when(storyNicknameSearchPort.findMemberUuidsByNickname("홍길동")).thenReturn(Set.of(matchedAuthor));

		var result = storyQueryUseCase.search(new SearchStoryQuery(
				StorySearchType.NICKNAME, " 홍길동 ", null, 0, 20
		));

		assertThat(result.items()).extracting(item -> item.storyUuid()).containsExactly(visible.storyUuid());
	}

	private StoryDetailView create(
			UUID authorUuid,
			String title,
			String country,
			String city,
			VisibilityScope visibilityScope
	) {
		return storyCommandUseCase.create(new CreateStoryCommand(
				authorUuid,
				null,
				false,
				title,
				"content",
				"https://img.example/cover.png",
				LocalDate.of(2026, 8, 1),
				LocalDate.of(2026, 8, 5),
				true,
				visibilityScope,
				false,
				List.of(new CreateStoryCommand.Country(
						country,
						0,
						List.of(new CreateStoryCommand.City(city, 0, List.of()))
				)),
				List.of(),
				List.of()
		));
	}
}
