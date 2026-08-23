package com.planwith.planwith_fo_story.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_story.application.command.CreateStoryCommand;
import com.planwith.planwith_fo_story.application.port.in.StoryCommandUseCase;
import com.planwith.planwith_fo_story.application.query.StoryDetailView;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class StoryQueryControllerAccessTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private StoryCommandUseCase storyCommandUseCase;

	@Test
	void missingStoryReturnsNotFound() throws Exception {
		mockMvc.perform(get("/api/stories/{storyUuid}", UUID.randomUUID()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("STORY_NOT_FOUND"));
	}

	@Test
	void privateStoryDeniedToOtherMemberReturnsForbidden() throws Exception {
		UUID authorUuid = UUID.randomUUID();
		StoryDetailView created = create(authorUuid, VisibilityScope.PRIVATE, List.of(UUID.randomUUID()));

		mockMvc.perform(get("/api/stories/{storyUuid}", created.storyUuid())
						.header("X-Member-UUID", UUID.randomUUID()))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("STORY_ACCESS_DENIED"));
	}

	@Test
	void memberStoryDeniedToGuestReturnsForbidden() throws Exception {
		UUID authorUuid = UUID.randomUUID();
		StoryDetailView created = create(authorUuid, VisibilityScope.MEMBER, List.of());

		mockMvc.perform(get("/api/stories/{storyUuid}", created.storyUuid()))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("STORY_ACCESS_DENIED"));
	}

	@Test
	void allStoryIsReadableWithoutLogin() throws Exception {
		UUID authorUuid = UUID.randomUUID();
		StoryDetailView created = create(authorUuid, VisibilityScope.ALL, List.of());

		mockMvc.perform(get("/api/stories/{storyUuid}", created.storyUuid()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.storyUuid").value(created.storyUuid()))
				.andExpect(jsonPath("$.visibilityScope").value("ALL"))
				.andExpect(jsonPath("$.countries[0].countryName").value("Korea"))
				.andExpect(jsonPath("$.countries[0].cities[0].cityName").value("Seoul"))
				.andExpect(jsonPath("$.visitCountries").doesNotExist())
				.andExpect(jsonPath("$.author").doesNotExist())
				.andExpect(jsonPath("$.visibilityMemberUuids").doesNotExist());
	}

	private StoryDetailView create(UUID authorUuid, VisibilityScope visibilityScope, List<UUID> visibilityMemberUuids) {
		return storyCommandUseCase.create(new CreateStoryCommand(
				authorUuid,
				null,
				false,
				"공개 스토리",
				"본문입니다.",
				"https://img.example/cover.png",
				LocalDate.of(2026, 8, 1),
				LocalDate.of(2026, 8, 5),
				true,
				visibilityScope,
				false,
				List.of(new CreateStoryCommand.Country(
						"Korea",
						0,
						List.of(new CreateStoryCommand.City("Seoul", 0, List.of()))
				)),
				List.of(),
				visibilityMemberUuids
		));
	}
}
