package com.planwith.planwith_fo_story.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "story.cache")
public class StoryCacheProperties {

	private String keyPrefix = "story";
	private String detailNamespace = "detail";
	private String popularNamespace = "popular";
	private String feedNamespace = "feed";
	private Duration ttl = Duration.ofMinutes(10);

	public String getKeyPrefix() {
		return keyPrefix;
	}

	public void setKeyPrefix(String keyPrefix) {
		this.keyPrefix = keyPrefix;
	}

	public String getDetailNamespace() {
		return detailNamespace;
	}

	public void setDetailNamespace(String detailNamespace) {
		this.detailNamespace = detailNamespace;
	}

	public String getPopularNamespace() {
		return popularNamespace;
	}

	public void setPopularNamespace(String popularNamespace) {
		this.popularNamespace = popularNamespace;
	}

	public String getFeedNamespace() {
		return feedNamespace;
	}

	public void setFeedNamespace(String feedNamespace) {
		this.feedNamespace = feedNamespace;
	}

	public Duration getTtl() {
		return ttl;
	}

	public void setTtl(Duration ttl) {
		this.ttl = ttl;
	}

	public Duration resolvedTtl() {
		return ttl == null || ttl.isZero() || ttl.isNegative()
				? Duration.ofMinutes(10)
				: ttl;
	}

	public String detailKey(String storyUuid) {
		return join(keyPrefix, detailNamespace, storyUuid);
	}

	public String popularKey() {
		return join(keyPrefix, popularNamespace);
	}

	public String feedKey(String memberUuid) {
		return join(keyPrefix, feedNamespace, memberUuid);
	}

	private static String join(String... segments) {
		return String.join(":", segments);
	}
}
