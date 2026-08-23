package com.planwith.planwith_fo_story.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "story.search")
public class StorySearchProperties {

	private boolean memberQueryEnabled;
	private String memberBaseUrl = "";
	private String memberSearchPath = "/api/v1/members/search";

	public boolean isMemberQueryEnabled() {
		return memberQueryEnabled;
	}

	public void setMemberQueryEnabled(boolean memberQueryEnabled) {
		this.memberQueryEnabled = memberQueryEnabled;
	}

	public String getMemberBaseUrl() {
		return memberBaseUrl;
	}

	public void setMemberBaseUrl(String memberBaseUrl) {
		this.memberBaseUrl = memberBaseUrl;
	}

	public String getMemberSearchPath() {
		return memberSearchPath;
	}

	public void setMemberSearchPath(String memberSearchPath) {
		this.memberSearchPath = memberSearchPath;
	}
}
