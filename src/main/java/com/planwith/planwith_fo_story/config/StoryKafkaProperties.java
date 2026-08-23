package com.planwith.planwith_fo_story.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "story.kafka")
public class StoryKafkaProperties {

	private boolean consumerEnabled = false;
	private Topics topics = new Topics();

	public boolean isConsumerEnabled() {
		return consumerEnabled;
	}

	public void setConsumerEnabled(boolean consumerEnabled) {
		this.consumerEnabled = consumerEnabled;
	}

	public Topics getTopics() {
		return topics;
	}

	public void setTopics(Topics topics) {
		this.topics = topics;
	}

	public static class Topics {
		private String memberProfileChanged = "planwith.member.profile-changed";
		private String likeCreated = "planwith.like.created";
		private String likeRemoved = "planwith.like.removed";
		private String membershipSubscribed = "planwith.membership.subscribed";
		private String membershipCanceled = "planwith.membership.canceled";
		private String storyCreated = "planwith.story.created";
		private String storyUpdated = "planwith.story.updated";
		private String storyDeleted = "planwith.story.deleted";

		public String getMemberProfileChanged() {
			return memberProfileChanged;
		}

		public void setMemberProfileChanged(String memberProfileChanged) {
			this.memberProfileChanged = memberProfileChanged;
		}

		public String getLikeCreated() {
			return likeCreated;
		}

		public void setLikeCreated(String likeCreated) {
			this.likeCreated = likeCreated;
		}

		public String getLikeRemoved() {
			return likeRemoved;
		}

		public void setLikeRemoved(String likeRemoved) {
			this.likeRemoved = likeRemoved;
		}

		public String getMembershipSubscribed() {
			return membershipSubscribed;
		}

		public void setMembershipSubscribed(String membershipSubscribed) {
			this.membershipSubscribed = membershipSubscribed;
		}

		public String getMembershipCanceled() {
			return membershipCanceled;
		}

		public void setMembershipCanceled(String membershipCanceled) {
			this.membershipCanceled = membershipCanceled;
		}

		public String getStoryCreated() {
			return storyCreated;
		}

		public void setStoryCreated(String storyCreated) {
			this.storyCreated = storyCreated;
		}

		public String getStoryUpdated() {
			return storyUpdated;
		}

		public void setStoryUpdated(String storyUpdated) {
			this.storyUpdated = storyUpdated;
		}

		public String getStoryDeleted() {
			return storyDeleted;
		}

		public void setStoryDeleted(String storyDeleted) {
			this.storyDeleted = storyDeleted;
		}
	}
}
