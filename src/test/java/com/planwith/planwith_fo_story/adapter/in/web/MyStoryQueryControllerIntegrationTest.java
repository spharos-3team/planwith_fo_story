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
class MyStoryQueryControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private StoryCommandUseCase storyCommandUseCase;

	@Test
	void returnsMyStoryListAndDetail() throws Exception {
		UUID ownerUuid = UUID.randomUUID();
		StoryDetailView story = storyCommandUseCase.create(CreateStoryCommandFactory.basic(
				ownerUuid,
				"My Story",
				"content",
				"https://img.example/cover.png",
				VisibilityScope.MEMBER
		));

		mockMvc.perform(get("/api/stories/me")
						.header("X-Auth-User-Id", ownerUuid)
						.param("country", "Korea")
						.param("city", "Seoul")
						.param("visibilityScope", "MEMBER"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].storyUuid").value(story.storyUuid()))
				.andExpect(jsonPath("$.items[0].coverImageUrl").value("https://img.example/cover.png"))
				.andExpect(jsonPath("$.items[0].title").value("My Story"))
				.andExpect(jsonPath("$.items[0].countries[0]").value("Korea"))
				.andExpect(jsonPath("$.items[0].cities[0]").value("Seoul"))
				.andExpect(jsonPath("$.items[0].createdAt").exists())
				.andExpect(jsonPath("$.items[0].storyLikeCount").value(0))
				.andExpect(jsonPath("$.items[0].storyCommentCount").value(0))
				.andExpect(jsonPath("$.items[0].viewCount").value(0))
				.andExpect(jsonPath("$.items[0].memberUuid").doesNotExist());

		mockMvc.perform(get("/api/stories/me/{storyUuid}", story.storyUuid())
						.header("X-Auth-User-Id", ownerUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.storyUuid").value(story.storyUuid()))
				.andExpect(jsonPath("$.title").value("My Story"));
	}

	@Test
	void requiresAuthenticationAndRejectsInvalidTravelPeriod() throws Exception {
		mockMvc.perform(get("/api/stories/me"))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(get("/api/stories/me")
						.header("X-Auth-User-Id", UUID.randomUUID())
						.param("travelStartDate", "2026-08-10")
						.param("travelEndDate", "2026-08-01"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_STORY_QUERY"));
	}

	@Test
	void deniesAnotherMembersStoryDetail() throws Exception {
		StoryDetailView story = storyCommandUseCase.create(CreateStoryCommandFactory.basic(
				UUID.randomUUID(),
				"Other Story",
				"content",
				"https://img.example/cover.png",
				VisibilityScope.ALL
		));

		mockMvc.perform(get("/api/stories/me/{storyUuid}", story.storyUuid())
						.header("X-Auth-User-Id", UUID.randomUUID()))
				.andExpect(status().isForbidden());
	}
}
