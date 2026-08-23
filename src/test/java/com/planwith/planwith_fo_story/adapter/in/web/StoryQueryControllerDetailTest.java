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
class StoryQueryControllerDetailTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private StoryCommandUseCase storyCommandUseCase;

	@Test
	void getDetailReturnsStoryDetailResponseShape() throws Exception {
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

		mockMvc.perform(get("/api/stories/{storyUuid}", created.storyUuid()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.storyUuid").value(created.storyUuid()))
				.andExpect(jsonPath("$.memberUuid").value(authorUuid.toString()))
				.andExpect(jsonPath("$.coverImageUrl").value("https://img.example/cover.png"))
				.andExpect(jsonPath("$.title").value("부산 여행"))
				.andExpect(jsonPath("$.content").value("해운대 기록을 남깁니다."))
				.andExpect(jsonPath("$.countries[0].countryName").value("Korea"))
				.andExpect(jsonPath("$.countries[0].cities[0].cityName").value("Busan"))
				.andExpect(jsonPath("$.places[0].placeName").value("해운대"))
				.andExpect(jsonPath("$.places[0].images[0].imageUrl").value("https://img.example/1.png"))
				.andExpect(jsonPath("$.startDate").value("2026-08-01"))
				.andExpect(jsonPath("$.endDate").value("2026-08-05"))
				.andExpect(jsonPath("$.tags[0]").value("여행"))
				.andExpect(jsonPath("$.commentEnabled").value(true))
				.andExpect(jsonPath("$.visibilityScope").value("ALL"))
				.andExpect(jsonPath("$.aiModerationStatus").value("UNVERIFIED"))
				.andExpect(jsonPath("$.viewCount").value(0))
				.andExpect(jsonPath("$.storyLikeCount").value(0))
				.andExpect(jsonPath("$.storyCommentCount").value(0))
				.andExpect(jsonPath("$.visitCountries").doesNotExist())
				.andExpect(jsonPath("$.author").doesNotExist())
				.andExpect(jsonPath("$.visibilityMemberUuids").doesNotExist());
	}
}
