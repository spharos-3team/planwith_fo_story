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
						.header("X-Auth-User-Id", UUID.randomUUID())
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
						.header("X-Auth-User-Id", UUID.randomUUID())
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
						.header("X-Auth-User-Id", UUID.randomUUID())
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
						.header("X-Auth-User-Id", UUID.randomUUID())
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
						.header("X-Auth-User-Id", UUID.randomUUID())
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
						.header("X-Auth-User-Id", UUID.randomUUID())
						.contentType(MediaType.APPLICATION_JSON)
						.content(validCreateBody()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("첫 스토리"))
				.andExpect(jsonPath("$.visitCountries[0].countryName").value("Korea"))
				.andExpect(jsonPath("$.visitCountries[0].cities[0].cityName").value("Seoul"))
				.andExpect(jsonPath("$.aiModerationStatus").value("UNVERIFIED"));
	}

	@Test
	void createPersistsNestedCountryCityPlaceImageAndTags() throws Exception {
		mockMvc.perform(post("/api/stories")
						.header("X-Auth-User-Id", UUID.randomUUID())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "일본 여행",
								  "content": "도쿄와 오사카 기록",
								  "coverImageUrl": "https://img.example/cover.png",
								  "startDate": "2026-08-01",
								  "endDate": "2026-08-05",
								  "commentEnabled": true,
								  "visibilityScope": "ALL",
								  "tags": ["여행", "일본"],
								  "countries": [
								    {
								      "countryName": "일본",
								      "cities": [
								        {
								          "cityName": "도쿄",
								          "places": [
								            {
								              "placeName": "시부야",
								              "images": [
								                { "imageUrl": "https://img.example/1.png", "imageOrder": 1 },
								                { "imageUrl": "https://img.example/2.png", "imageOrder": 2 },
								                { "imageUrl": "https://img.example/3.png", "imageOrder": 3 }
								              ]
								            },
								            { "placeName": "도쿄타워" }
								          ]
								        },
								        {
								          "cityName": "오사카",
								          "places": [
								            { "placeName": "도톤보리" }
								          ]
								        }
								      ]
								    }
								  ]
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.visitCountries[0].cities[0].cityName").value("도쿄"))
				.andExpect(jsonPath("$.visitCountries[0].cities[0].places[0].placeName").value("시부야"))
				.andExpect(jsonPath("$.visitCountries[0].cities[0].places[0].images.length()").value(3))
				.andExpect(jsonPath("$.visitCountries[0].cities[0].places[1].placeName").value("도쿄타워"))
				.andExpect(jsonPath("$.visitCountries[0].cities[1].places[0].placeName").value("도톤보리"))
				.andExpect(jsonPath("$.tags[0]").value("여행"))
				.andExpect(jsonPath("$.tags[1]").value("일본"));
	}

	@Test
	void createWithSixPlaceImagesReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/stories")
						.header("X-Auth-User-Id", UUID.randomUUID())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "제목",
								  "content": "본문입니다.",
								  "coverImageUrl": "https://img.example/cover.png",
								  "startDate": "2026-08-01",
								  "endDate": "2026-08-05",
								  "commentEnabled": true,
								  "visibilityScope": "ALL",
								  "countries": [
								    {
								      "countryName": "일본",
								      "cities": [
								        {
								          "cityName": "도쿄",
								          "places": [
								            {
								              "placeName": "시부야",
								              "images": [
								                { "imageUrl": "https://img.example/1.png", "imageOrder": 1 },
								                { "imageUrl": "https://img.example/2.png", "imageOrder": 2 },
								                { "imageUrl": "https://img.example/3.png", "imageOrder": 3 },
								                { "imageUrl": "https://img.example/4.png", "imageOrder": 4 },
								                { "imageUrl": "https://img.example/5.png", "imageOrder": 5 },
								                { "imageUrl": "https://img.example/6.png", "imageOrder": 1 }
								              ]
								            }
								          ]
								        }
								      ]
								    }
								  ]
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
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
