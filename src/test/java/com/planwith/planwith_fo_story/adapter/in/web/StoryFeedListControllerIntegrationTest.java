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
import com.planwith.planwith_fo_story.application.port.out.StoryCounterPort;
import com.planwith.planwith_fo_story.application.query.StoryDetailView;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class StoryFeedListControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private StoryCommandUseCase storyCommandUseCase;

	@Autowired
	private StoryCounterPort storyCounterPort;

	@Test
	void returnsFlatStoryListItemsWithRequestedSort() throws Exception {
		StoryDetailView created = storyCommandUseCase.create(CreateStoryCommandFactory.basic(
				UUID.randomUUID(),
				"Feed story",
				"content",
				"https://img.example/cover.png",
				VisibilityScope.ALL
		));
		storyCounterPort.incrementViewCount(UUID.fromString(created.storyUuid()));

		mockMvc.perform(get("/api/stories")
						.param("sort", "VIEW")
						.param("page", "0")
						.param("size", "20"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].storyUuid").value(created.storyUuid()))
				.andExpect(jsonPath("$.items[0].title").value("Feed story"))
				.andExpect(jsonPath("$.items[0].countries[0]").value("Korea"))
				.andExpect(jsonPath("$.items[0].cities[0]").value("Seoul"))
				.andExpect(jsonPath("$.items[0].viewCount").value(1))
				.andExpect(jsonPath("$.items[0].visibilityScope").doesNotExist())
				.andExpect(jsonPath("$.items[0].author").doesNotExist());
	}
}
