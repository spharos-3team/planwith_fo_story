package com.planwith.planwith_fo_story.adapter.out.feed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.planwith.planwith_fo_story.config.StoryFeedProperties;

class RestStoryFeedMemberQueryAdapterTest {

	@Test
	void keepsOnlyAdventureAndPlanwithFollowingAuthors() {
		UUID viewerUuid = UUID.randomUUID();
		UUID adventureUuid = UUID.randomUUID();
		UUID travelerUuid = UUID.randomUUID();
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		StoryFeedProperties properties = new StoryFeedProperties();
		properties.setMemberBaseUrl("http://member-service");
		RestStoryFeedMemberQueryAdapter adapter = new RestStoryFeedMemberQueryAdapter(builder, properties);

		server.expect(requestTo("http://member-service/api/v1/members/%s/followings?page=0&size=50".formatted(viewerUuid)))
				.andRespond(withSuccess("""
						{"success":true,"data":{"content":[
						{"memberUuid":"%s","grade":"ADVENTURE"},
						{"memberUuid":"%s","grade":"TRAVELER"}
						],"page":0,"size":50,"totalElements":2,"totalPages":1}}
						""".formatted(adventureUuid, travelerUuid), MediaType.APPLICATION_JSON));

		assertThat(adapter.findEligibleFollowingAuthors(viewerUuid)).contains(java.util.Set.of(adventureUuid));
		server.verify();
	}
}
