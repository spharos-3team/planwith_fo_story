package com.planwith.planwith_fo_story.adapter.out.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.planwith.planwith_fo_story.config.StorySearchProperties;

class RestStoryNicknameSearchAdapterTest {

	@Test
	void resolvesAllMemberUuidsFromNicknameSearch() {
		UUID firstMemberUuid = UUID.randomUUID();
		UUID secondMemberUuid = UUID.randomUUID();
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		StorySearchProperties properties = new StorySearchProperties();
		properties.setMemberBaseUrl("http://member-service");
		RestStoryNicknameSearchAdapter adapter = new RestStoryNicknameSearchAdapter(builder, properties);

		server.expect(requestTo(
				"http://member-service/api/v1/members/search?nickname=%ED%99%8D%EA%B8%B8%EB%8F%99&page=0&size=50"
		)).andRespond(withSuccess("""
				{"success":true,"data":{"content":[
				{"memberUuid":"%s","nickname":"홍길동"},
				{"memberUuid":"%s","nickname":"홍길동2"}
				],"page":0,"size":50,"totalElements":2,"totalPages":1}}
				""".formatted(firstMemberUuid, secondMemberUuid), MediaType.APPLICATION_JSON));

		assertThat(adapter.findMemberUuidsByNickname("홍길동"))
				.containsExactlyInAnyOrder(firstMemberUuid, secondMemberUuid);
		server.verify();
	}
}
