package com.planwith.planwith_fo_story.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_story.application.command.CreateStoryCommandFactory;
import com.planwith.planwith_fo_story.application.port.in.StoryCommandUseCase;
import com.planwith.planwith_fo_story.application.query.StoryDetailView;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class StorySearchControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private StoryCommandUseCase storyCommandUseCase;

	@Test
	void returnsStoryListItemsForCountrySearch() throws Exception {
		StoryDetailView story = storyCommandUseCase.create(CreateStoryCommandFactory.basic(
				UUID.randomUUID(),
				"Search story",
				"content",
				"https://img.example/cover.png",
				VisibilityScope.ALL
		));

		mockMvc.perform(get("/api/stories/search")
						.param("type", "COUNTRY")
						.param("keyword", "Kor")
						.param("page", "0")
						.param("size", "20"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].storyUuid").value(story.storyUuid()))
				.andExpect(jsonPath("$.items[0].title").value("Search story"))
				.andExpect(jsonPath("$.items[0].countries[0]").value("Korea"))
				.andExpect(jsonPath("$.items[0].cities[0]").value("Seoul"));
	}

	@Test
	void rejectsBlankSearchKeyword() throws Exception {
		mockMvc.perform(get("/api/stories/search")
						.param("type", "CITY")
						.param("keyword", " "))
				.andExpect(status().isBadRequest());
	}
}
