package com.planwith.planwith_fo_story.adapter.out.feed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.planwith.planwith_fo_story.config.StoryFeedProperties;

class RestStoryFeedMembershipQueryAdapterTest {

	@Test
	void returnsOnlyActiveJoinedMembershipCreators() {
		UUID viewerUuid = UUID.randomUUID();
		UUID activeCreatorUuid = UUID.randomUUID();
		UUID inactiveCreatorUuid = UUID.randomUUID();
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		StoryFeedProperties properties = new StoryFeedProperties();
		properties.setMembershipBaseUrl("http://membership-service");
		RestStoryFeedMembershipQueryAdapter adapter = new RestStoryFeedMembershipQueryAdapter(builder, properties);

		server.expect(requestTo("http://membership-service/api/planwith-fo-membership/memberships/me/subscriptions"))
				.andExpect(header("X-Auth-User-Id", viewerUuid.toString()))
				.andRespond(withSuccess("""
						[
						{"creatorUuid":"%s","status":"ACTIVE"},
						{"creatorUuid":"%s","status":"INACTIVE"}
						]
						""".formatted(activeCreatorUuid, inactiveCreatorUuid), MediaType.APPLICATION_JSON));

		assertThat(adapter.findJoinedCreatorUuids(viewerUuid)).containsExactly(activeCreatorUuid);
		server.verify();
	}
}
