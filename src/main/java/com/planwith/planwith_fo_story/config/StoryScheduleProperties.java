package com.planwith.planwith_fo_story.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "story.schedule")
public class StoryScheduleProperties {

	private boolean ownershipCheckEnabled = false;
	private String baseUrl = "";
	/**
	 * Schedule Service 소유 확인 경로.
	 * 현재 저장소에 Schedule API 계약이 없어 가정한 경로이며, ownership-check-enabled=true일 때만 사용한다.
	 */
	private String ownershipPath = "/api/schedules/{scheduleUuid}/owners/{memberUuid}";

	public boolean isOwnershipCheckEnabled() {
		return ownershipCheckEnabled;
	}

	public void setOwnershipCheckEnabled(boolean ownershipCheckEnabled) {
		this.ownershipCheckEnabled = ownershipCheckEnabled;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getOwnershipPath() {
		return ownershipPath;
	}

	public void setOwnershipPath(String ownershipPath) {
		this.ownershipPath = ownershipPath;
	}
}
