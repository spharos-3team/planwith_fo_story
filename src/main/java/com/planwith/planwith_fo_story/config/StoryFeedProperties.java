package com.planwith.planwith_fo_story.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "story.feed")
public class StoryFeedProperties {

	private boolean memberQueryEnabled;
	private String memberBaseUrl = "";
	private String followingsPath = "/api/v1/members/{memberUuid}/followings";
	private String membersPath = "/api/v1/members/search";
	private boolean membershipQueryEnabled;
	private String membershipBaseUrl = "";
	private String joinedMembershipsPath = "/api/planwith-fo-membership/memberships/me/subscriptions";
	private List<String> eligibleGrades = List.of("ADVENTURE", "PLANWITH");

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

	public String getFollowingsPath() {
		return followingsPath;
	}

	public void setFollowingsPath(String followingsPath) {
		this.followingsPath = followingsPath;
	}

	public String getMembersPath() {
		return membersPath;
	}

	public void setMembersPath(String membersPath) {
		this.membersPath = membersPath;
	}

	public boolean isMembershipQueryEnabled() {
		return membershipQueryEnabled;
	}

	public void setMembershipQueryEnabled(boolean membershipQueryEnabled) {
		this.membershipQueryEnabled = membershipQueryEnabled;
	}

	public String getMembershipBaseUrl() {
		return membershipBaseUrl;
	}

	public void setMembershipBaseUrl(String membershipBaseUrl) {
		this.membershipBaseUrl = membershipBaseUrl;
	}

	public String getJoinedMembershipsPath() {
		return joinedMembershipsPath;
	}

	public void setJoinedMembershipsPath(String joinedMembershipsPath) {
		this.joinedMembershipsPath = joinedMembershipsPath;
	}

	public List<String> getEligibleGrades() {
		return eligibleGrades;
	}

	public void setEligibleGrades(List<String> eligibleGrades) {
		this.eligibleGrades = eligibleGrades == null ? List.of() : List.copyOf(eligibleGrades);
	}
}
