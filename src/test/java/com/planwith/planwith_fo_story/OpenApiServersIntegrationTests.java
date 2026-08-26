package com.planwith.planwith_fo_story;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@AutoConfigureMockMvc
class OpenApiServersIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void apiDocsPublishesGatewayServerAndBearerSecurityScheme() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.servers[0].url").value("/"))
				.andExpect(jsonPath("$.servers[0].description").value("API Gateway"))
				.andExpect(jsonPath("$.components.securitySchemes.Bearer.type").value("http"))
				.andExpect(jsonPath("$.components.securitySchemes.Bearer.scheme").value("bearer"))
				.andExpect(jsonPath("$.components.securitySchemes.Bearer.bearerFormat").value("JWT"))
				.andExpect(jsonPath("$.security[0].Bearer").isArray());
	}
}
