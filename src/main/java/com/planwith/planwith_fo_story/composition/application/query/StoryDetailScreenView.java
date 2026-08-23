package com.planwith.planwith_fo_story.composition.application.query;

import java.util.List;

import com.planwith.planwith_fo_story.application.query.StoryDetailView;
import com.planwith.planwith_fo_story.composition.domain.CommentUiState;

public record StoryDetailScreenView(
		StoryDetailView story,
		MemberScreenView member,
		FollowScreenView follow,
		ScheduleScreenView schedule,
		LikeScreenView like,
		CommentScreenView comment,
		MembershipScreenView membership
) {
	public record MemberScreenView(
			String memberUuid,
			String nickname,
			String profileImageUrl,
			String bio
	) {
	}

	public record FollowScreenView(
			long followerCount,
			long followingCount
	) {
	}

	public record ScheduleScreenView(
			String scheduleUuid,
			String title,
			List<ScheduleDailyPlanView> dailyPlans,
			List<ScheduleFlightView> flights
	) {
	}

	public record LikeScreenView(
			boolean liked,
			long storyLikeCount
	) {
	}

	public record CommentScreenView(
			CommentUiState uiState,
			String message,
			List<StoryCommentItemView> items
	) {
	}

	public record MembershipScreenView(
			boolean subscribed
	) {
	}
}
