package com.planwith.planwith_fo_story.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class StoryCommandControllerValidationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void createWithoutMemberHeaderReturnsUnauthorized() throws Exception {
		mockMvc.perform(post("/api/stories")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validCreateBody()))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("MEMBER_AUTHENTICATION_REQUIRED"));
	}

	@Test
	void createWithoutTitleReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/stories")
						.header("X-Member-UUID", UUID.randomUUID())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "",
								  "content": "본문입니다.",
								  "coverImageUrl": "https://img.example/cover.png",
								  "startDate": "2026-08-01",
								  "endDate": "2026-08-05",
								  "commentEnabled": true,
								  "visibilityScope": "ALL"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
	}

	@Test
	void createWithoutCommentEnabledReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/stories")
						.header("X-Member-UUID", UUID.randomUUID())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "제목",
								  "content": "본문입니다.",
								  "coverImageUrl": "https://img.example/cover.png",
								  "startDate": "2026-08-01",
								  "endDate": "2026-08-05",
								  "visibilityScope": "ALL"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
	}

	@Test
	void createWithScheduleVisibleWithoutScheduleUuidReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/stories")
						.header("X-Member-UUID", UUID.randomUUID())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "scheduleVisible": true,
								  "title": "제목",
								  "content": "본문입니다.",
								  "coverImageUrl": "https://img.example/cover.png",
								  "startDate": "2026-08-01",
								  "endDate": "2026-08-05",
								  "commentEnabled": true,
								  "visibilityScope": "ALL",
								  "countries": [
								    {
								      "countryName": "Korea",
								      "cities": [
								        { "cityName": "Seoul" }
								      ]
								    }
								  ]
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_STORY_STATE"));
	}

	@Test
	void createWithVideoCoverReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/stories")
						.header("X-Member-UUID", UUID.randomUUID())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "제목",
								  "content": "본문입니다.",
								  "coverImageUrl": "https://img.example/cover.mp4",
								  "startDate": "2026-08-01",
								  "endDate": "2026-08-05",
								  "commentEnabled": true,
								  "visibilityScope": "ALL",
								  "countries": [
								    {
								      "countryName": "Korea",
								      "cities": [
								        { "cityName": "Seoul" }
								      ]
								    }
								  ]
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_STORY_STATE"));
	}

	@Test
	void createWithoutCountriesReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/stories")
						.header("X-Member-UUID", UUID.randomUUID())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "제목",
								  "content": "본문입니다.",
								  "coverImageUrl": "https://img.example/cover.png",
								  "startDate": "2026-08-01",
								  "endDate": "2026-08-05",
								  "commentEnabled": true,
								  "visibilityScope": "ALL"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
	}

	@Test
	void createWithValidRequestSucceeds() throws Exception {
		mockMvc.perform(post("/api/stories")
						.header("X-Member-UUID", UUID.randomUUID())
						.contentType(MediaType.APPLICATION_JSON)
						.content(validCreateBody()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("첫 스토리"))
				.andExpect(jsonPath("$.visitCountries[0].countryName").value("Korea"))
				.andExpect(jsonPath("$.visitCountries[0].cities[0].cityName").value("Seoul"))
				.andExpect(jsonPath("$.aiModerationStatus").value("UNVERIFIED"));
	}

	private static String validCreateBody() {
		return """
				{
				  "title": "첫 스토리",
				  "content": "본문입니다.",
				  "coverImageUrl": "https://img.example/cover.png",
				  "startDate": "2026-08-01",
				  "endDate": "2026-08-05",
				  "commentEnabled": true,
				  "visibilityScope": "ALL",
				  "countries": [
				    {
				      "countryName": "Korea",
				      "cities": [
				        { "cityName": "Seoul" }
				      ]
				    }
				  ]
				}
				""";
	}
}
