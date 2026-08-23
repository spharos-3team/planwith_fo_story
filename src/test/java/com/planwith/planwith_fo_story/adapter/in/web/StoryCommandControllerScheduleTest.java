package com.planwith.planwith_fo_story.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_story.application.port.out.ScheduleOwnershipPort;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class StoryCommandControllerScheduleTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ScheduleOwnershipPort scheduleOwnershipPort;

	@Test
	void createWithUnownedScheduleReturnsBadRequest() throws Exception {
		when(scheduleOwnershipPort.isOwnedBy(any(), any())).thenReturn(false);

		mockMvc.perform(post("/api/stories")
						.header("X-Member-UUID", UUID.randomUUID())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "scheduleUuid": "%s",
								  "scheduleVisible": false,
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
								""".formatted(UUID.randomUUID())))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("SCHEDULE_NOT_OWNED"));
	}
}
