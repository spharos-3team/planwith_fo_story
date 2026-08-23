package com.planwith.planwith_fo_story.composition.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "story.detail-screen")
public class StoryDetailScreenProperties {

	private Member member = new Member();
	private Follow follow = new Follow();
	private Schedule schedule = new Schedule();
	private Like like = new Like();
	private Comment comment = new Comment();

	public Member getMember() {
		return member;
	}

	public void setMember(Member member) {
		this.member = member;
	}

	public Follow getFollow() {
		return follow;
	}

	public void setFollow(Follow follow) {
		this.follow = follow;
	}

	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	public Like getLike() {
		return like;
	}

	public void setLike(Like like) {
		this.like = like;
	}

	public Comment getComment() {
		return comment;
	}

	public void setComment(Comment comment) {
		this.comment = comment;
	}

	public static class Member {

		private boolean queryEnabled = false;
		private String baseUrl = "";
		/**
		 * Member Service 프로필 조회 경로(가정).
		 * query-enabled=true일 때만 사용한다.
		 */
		private String profilePath = "/api/members/{memberUuid}/profile";

		public boolean isQueryEnabled() {
			return queryEnabled;
		}

		public void setQueryEnabled(boolean queryEnabled) {
			this.queryEnabled = queryEnabled;
		}

		public String getBaseUrl() {
			return baseUrl;
		}

		public void setBaseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
		}

		public String getProfilePath() {
			return profilePath;
		}

		public void setProfilePath(String profilePath) {
			this.profilePath = profilePath;
		}
	}

	public static class Follow {

		private boolean queryEnabled = false;
		private String baseUrl = "";
		/**
		 * Follow Service 팔로우 요약 조회 경로(가정).
		 */
		private String summaryPath = "/api/members/{memberUuid}/follow-summary";

		public boolean isQueryEnabled() {
			return queryEnabled;
		}

		public void setQueryEnabled(boolean queryEnabled) {
			this.queryEnabled = queryEnabled;
		}

		public String getBaseUrl() {
			return baseUrl;
		}

		public void setBaseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
		}

		public String getSummaryPath() {
			return summaryPath;
		}

		public void setSummaryPath(String summaryPath) {
			this.summaryPath = summaryPath;
		}
	}

	public static class Schedule {

		private boolean queryEnabled = false;
		private String baseUrl = "";
		/**
		 * Schedule Service 상세 조회 경로(가정).
		 */
		private String detailPath = "/api/schedules/{scheduleUuid}";

		public boolean isQueryEnabled() {
			return queryEnabled;
		}

		public void setQueryEnabled(boolean queryEnabled) {
			this.queryEnabled = queryEnabled;
		}

		public String getBaseUrl() {
			return baseUrl;
		}

		public void setBaseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
		}

		public String getDetailPath() {
			return detailPath;
		}

		public void setDetailPath(String detailPath) {
			this.detailPath = detailPath;
		}
	}

	public static class Like {

		private boolean queryEnabled = false;
		private String baseUrl = "";
		/**
		 * Like Service 조회자 좋아요 여부 경로(가정).
		 */
		private String statusPath = "/api/stories/{storyUuid}/likes/me";

		public boolean isQueryEnabled() {
			return queryEnabled;
		}

		public void setQueryEnabled(boolean queryEnabled) {
			this.queryEnabled = queryEnabled;
		}

		public String getBaseUrl() {
			return baseUrl;
		}

		public void setBaseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
		}

		public String getStatusPath() {
			return statusPath;
		}

		public void setStatusPath(String statusPath) {
			this.statusPath = statusPath;
		}
	}

	public static class Comment {

		private boolean queryEnabled = false;
		private String baseUrl = "";
		/**
		 * Comment Service 댓글 목록 조회 경로(가정).
		 */
		private String listPath = "/api/stories/{storyUuid}/comments";

		public boolean isQueryEnabled() {
			return queryEnabled;
		}

		public void setQueryEnabled(boolean queryEnabled) {
			this.queryEnabled = queryEnabled;
		}

		public String getBaseUrl() {
			return baseUrl;
		}

		public void setBaseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
		}

		public String getListPath() {
			return listPath;
		}

		public void setListPath(String listPath) {
			this.listPath = listPath;
		}
	}
}
