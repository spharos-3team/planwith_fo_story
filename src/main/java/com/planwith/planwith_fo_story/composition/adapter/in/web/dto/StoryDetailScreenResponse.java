package com.planwith.planwith_fo_story.composition.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.planwith.planwith_fo_story.composition.application.query.ScheduleDailyPlanView;
import com.planwith.planwith_fo_story.composition.application.query.ScheduleFlightView;
import com.planwith.planwith_fo_story.composition.application.query.StoryCommentItemView;
import com.planwith.planwith_fo_story.composition.application.query.StoryDetailScreenView;
import com.planwith.planwith_fo_story.composition.application.query.StoryDetailScreenView.CommentScreenView;
import com.planwith.planwith_fo_story.composition.application.query.StoryDetailScreenView.FollowScreenView;
import com.planwith.planwith_fo_story.composition.application.query.StoryDetailScreenView.LikeScreenView;
import com.planwith.planwith_fo_story.composition.application.query.StoryDetailScreenView.MemberScreenView;
import com.planwith.planwith_fo_story.composition.application.query.StoryDetailScreenView.MembershipScreenView;
import com.planwith.planwith_fo_story.composition.application.query.StoryDetailScreenView.ScheduleScreenView;
import com.planwith.planwith_fo_story.composition.domain.CommentUiState;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "스토리 상세 화면 통합 조회 응답")
public record StoryDetailScreenResponse(
		StorySection story,
		MemberSection member,
		FollowSection follow,
		ScheduleSection schedule,
		LikeSection like,
		CommentSection comment,
		MembershipSection membership
) {
	public static StoryDetailScreenResponse from(StoryDetailScreenView view) {
		return new StoryDetailScreenResponse(
				StorySection.from(view.story()),
				MemberSection.from(view.member()),
				FollowSection.from(view.follow()),
				view.schedule() == null ? null : ScheduleSection.from(view.schedule()),
				LikeSection.from(view.like()),
				CommentSection.from(view.comment()),
				MembershipSection.from(view.membership())
		);
	}

	@Schema(description = "스토리 본문 영역")
	public record StorySection(
			String storyUuid,
			String memberUuid,
			String scheduleUuid,
			boolean scheduleVisible,
			String coverImageUrl,
			String title,
			String content,
			List<com.planwith.planwith_fo_story.application.query.StoryVisitCountryView> countries,
			List<com.planwith.planwith_fo_story.application.query.StoryPlaceView> places,
			java.time.LocalDate startDate,
			java.time.LocalDate endDate,
			List<String> tags,
			boolean commentEnabled,
			com.planwith.planwith_fo_story.domain.model.VisibilityScope visibilityScope,
			com.planwith.planwith_fo_story.domain.model.AiModerationStatus aiModerationStatus,
			long viewCount,
			long storyLikeCount,
			long storyCommentCount,
			LocalDateTime createdAt,
			LocalDateTime updatedAt
	) {
		public static StorySection from(com.planwith.planwith_fo_story.application.query.StoryDetailView story) {
			return new StorySection(
					story.storyUuid(),
					story.memberUuid(),
					story.scheduleUuid(),
					story.scheduleVisible(),
					story.coverImageUrl(),
					story.title(),
					story.content(),
					story.visitCountries(),
					story.places(),
					story.startDate(),
					story.endDate(),
					story.tags(),
					story.commentEnabled(),
					story.visibilityScope(),
					story.aiModerationStatus(),
					story.viewCount(),
					story.storyLikeCount(),
					story.storyCommentCount(),
					story.createdAt(),
					story.updatedAt()
			);
		}
	}

	@Schema(description = "작성자 프로필 영역")
	public record MemberSection(
			String memberUuid,
			String nickname,
			String profileImageUrl,
			String bio
	) {
		public static MemberSection from(MemberScreenView member) {
			return new MemberSection(
					member.memberUuid(),
					member.nickname(),
					member.profileImageUrl(),
					member.bio()
			);
		}
	}

	@Schema(description = "팔로우 요약 영역")
	public record FollowSection(
			long followerCount,
			long followingCount
	) {
		public static FollowSection from(FollowScreenView follow) {
			return new FollowSection(follow.followerCount(), follow.followingCount());
		}
	}

	@Schema(description = "일정 영역")
	public record ScheduleSection(
			String scheduleUuid,
			String title,
			List<ScheduleDailyPlanView> dailyPlans,
			List<ScheduleFlightView> flights
	) {
		public static ScheduleSection from(ScheduleScreenView schedule) {
			return new ScheduleSection(
					schedule.scheduleUuid(),
					schedule.title(),
					schedule.dailyPlans(),
					schedule.flights()
			);
		}
	}

	@Schema(description = "좋아요 영역")
	public record LikeSection(
			boolean liked,
			long storyLikeCount
	) {
		public static LikeSection from(LikeScreenView like) {
			return new LikeSection(like.liked(), like.storyLikeCount());
		}
	}

	@Schema(description = "댓글 영역")
	public record CommentSection(
			CommentUiState uiState,
			String message,
			List<StoryCommentItemView> items
	) {
		public static CommentSection from(CommentScreenView comment) {
			return new CommentSection(comment.uiState(), comment.message(), comment.items());
		}
	}

	@Schema(description = "멤버십 영역")
	public record MembershipSection(
			boolean subscribed
	) {
		public static MembershipSection from(MembershipScreenView membership) {
			return new MembershipSection(membership.subscribed());
		}
	}
}
