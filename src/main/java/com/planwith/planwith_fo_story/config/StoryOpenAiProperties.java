package com.planwith.planwith_fo_story.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "story.openai")
public class StoryOpenAiProperties {

	private boolean enabled = false;
	private String apiKey = "";
	private String baseUrl = "https://api.openai.com";
	private String model = "omni-moderation-latest";
	private String moderationPath = "/v1/moderations";

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getModerationPath() {
		return moderationPath;
	}

	public void setModerationPath(String moderationPath) {
		this.moderationPath = moderationPath;
	}
}
