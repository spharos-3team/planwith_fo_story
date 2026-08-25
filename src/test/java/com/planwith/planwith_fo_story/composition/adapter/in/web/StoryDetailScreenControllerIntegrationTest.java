package com.planwith.planwith_fo_story.composition.adapter.in.web;

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
import com.planwith.planwith_fo_story.composition.domain.CommentUiPolicy;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class StoryDetailScreenControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private StoryCommandUseCase storyCommandUseCase;

	@Test
	void getDetailScreenReturnsComposedResponseForGuest() throws Exception {
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

		mockMvc.perform(get("/api/bff/stories/{storyUuid}", created.storyUuid()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.story.storyUuid").value(created.storyUuid()))
				.andExpect(jsonPath("$.story.title").value("부산 여행"))
				.andExpect(jsonPath("$.member.memberUuid").value(authorUuid.toString()))
				.andExpect(jsonPath("$.follow.followerCount").value(0))
				.andExpect(jsonPath("$.follow.followingCount").value(0))
				.andExpect(jsonPath("$.like.liked").value(false))
				.andExpect(jsonPath("$.like.storyLikeCount").value(0))
				.andExpect(jsonPath("$.comment.uiState").value("LOGIN_REQUIRED"))
				.andExpect(jsonPath("$.membership.subscribed").value(false))
				.andExpect(jsonPath("$.schedule").doesNotExist());
	}

	@Test
	void getDetailScreenReturnsDisabledCommentWhenCommentDisabled() throws Exception {
		UUID authorUuid = UUID.randomUUID();
		StoryDetailView created = storyCommandUseCase.create(new CreateStoryCommand(
				authorUuid,
				null,
				false,
				"댓글 중지",
				"댓글을 사용하지 않습니다.",
				"https://img.example/cover.png",
				LocalDate.of(2026, 8, 1),
				LocalDate.of(2026, 8, 2),
				false,
				VisibilityScope.ALL,
				false,
				List.of(new CreateStoryCommand.Country(
						"Korea",
						0,
						List.of(new CreateStoryCommand.City(
								"Seoul",
								0,
								List.of(new CreateStoryCommand.Place(
										"명동",
										0,
										List.of(new CreateStoryCommand.PlaceImage("https://img.example/1.png", 1))
								))
						))
				)),
				List.of(),
				List.of()
		));

		mockMvc.perform(get("/api/bff/stories/{storyUuid}", created.storyUuid())
						.header("X-Auth-User-Id", UUID.randomUUID()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.comment.uiState").value("DISABLED"))
				.andExpect(jsonPath("$.comment.message").value(CommentUiPolicy.DISABLED_MESSAGE))
				.andExpect(jsonPath("$.comment.items").isEmpty());
	}
}
