package com.planwith.planwith_fo_story.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.planwith.planwith_fo_story.application.command.CreateStoryCommandFactory;
import com.planwith.planwith_fo_story.application.port.in.StoryCommandUseCase;
import com.planwith.planwith_fo_story.application.query.StoryDetailView;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class StoryUpdateDeleteControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private StoryCommandUseCase storyCommandUseCase;

	@Test
	void patchesAllEditableFieldsAndSoftDeletesStory() throws Exception {
		UUID authorUuid = UUID.randomUUID();
		StoryDetailView created = storyCommandUseCase.create(CreateStoryCommandFactory.basic(
				authorUuid, "Original", "content", "https://img.example/cover.png", VisibilityScope.ALL
		));

		mockMvc.perform(patch("/api/stories/{storyUuid}", created.storyUuid())
						.header("X-Member-UUID", authorUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title":"Updated",
								  "content":"Updated content",
								  "coverImageUrl":"https://img.example/updated.png",
								  "startDate":"2026-09-01",
								  "endDate":"2026-09-10",
								  "commentEnabled":false,
								  "visibilityScope":"ALL",
								  "scheduleVisible":false,
								  "aiVerificationRequested":false,
								  "countries":[{"countryName":"Japan","cities":[{
								    "cityName":"Tokyo","places":[{"placeName":"Tower","images":[{
									      "imageUrl":"https://img.example/tower.png","imageOrder":1
								    }]}]
								  }]}],
								  "tags":["japan"],
								  "visibilityMemberUuids":[]
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.storyUuid").value(created.storyUuid()))
				.andExpect(jsonPath("$.memberUuid").value(authorUuid.toString()))
				.andExpect(jsonPath("$.title").value("Updated"))
				.andExpect(jsonPath("$.commentEnabled").value(false))
				.andExpect(jsonPath("$.visitCountries[0].countryName").value("Japan"))
				.andExpect(jsonPath("$.visitCountries[0].cities[0].cityName").value("Tokyo"))
				.andExpect(jsonPath("$.tags[0]").value("japan"));

		mockMvc.perform(delete("/api/stories/{storyUuid}", created.storyUuid())
						.header("X-Member-UUID", authorUuid))
				.andExpect(status().isNoContent());
	}

	@Test
	void rejectsAnotherMembersPatchAndDelete() throws Exception {
		UUID authorUuid = UUID.randomUUID();
		UUID otherUuid = UUID.randomUUID();
		StoryDetailView created = storyCommandUseCase.create(CreateStoryCommandFactory.basic(
				authorUuid, "Original", "content", "https://img.example/cover.png", VisibilityScope.ALL
		));

		mockMvc.perform(patch("/api/stories/{storyUuid}", created.storyUuid())
						.header("X-Member-UUID", otherUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content(validUpdateBody()))
				.andExpect(status().isForbidden());
		mockMvc.perform(delete("/api/stories/{storyUuid}", created.storyUuid())
						.header("X-Member-UUID", otherUuid))
				.andExpect(status().isForbidden());
	}

	private static String validUpdateBody() {
		return """
				{
				  "title":"Updated","content":"Updated content",
				  "coverImageUrl":"https://img.example/updated.png",
				  "startDate":"2026-09-01","endDate":"2026-09-10",
				  "commentEnabled":true,"visibilityScope":"ALL",
				  "countries":[{"countryName":"Japan","cities":[{"cityName":"Tokyo","places":[]}]}]
				}
				""";
	}
}
