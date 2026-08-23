package com.planwith.planwith_fo_story.composition.application.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.planwith.planwith_fo_story.application.port.in.StoryQueryUseCase;
import com.planwith.planwith_fo_story.application.port.out.MembershipEntitlementProjectionPort;
import com.planwith.planwith_fo_story.application.query.GetStoryDetailQuery;
import com.planwith.planwith_fo_story.application.query.StoryAuthorView;
import com.planwith.planwith_fo_story.application.query.StoryDetailView;
import com.planwith.planwith_fo_story.composition.application.port.in.StoryDetailScreenUseCase;
import com.planwith.planwith_fo_story.composition.application.port.out.FollowSummaryQueryPort;
import com.planwith.planwith_fo_story.composition.application.port.out.MemberBioQueryPort;
import com.planwith.planwith_fo_story.composition.application.port.out.ScheduleDetailQueryPort;
import com.planwith.planwith_fo_story.composition.application.port.out.StoryCommentListQueryPort;
import com.planwith.planwith_fo_story.composition.application.port.out.StoryLikeStatusQueryPort;
import com.planwith.planwith_fo_story.composition.application.query.GetStoryDetailScreenQuery;
import com.planwith.planwith_fo_story.composition.application.query.StoryCommentItemView;
import com.planwith.planwith_fo_story.composition.application.query.StoryDetailScreenView;
import com.planwith.planwith_fo_story.composition.application.query.StoryDetailScreenView.CommentScreenView;
import com.planwith.planwith_fo_story.composition.application.query.StoryDetailScreenView.FollowScreenView;
import com.planwith.planwith_fo_story.composition.application.query.StoryDetailScreenView.LikeScreenView;
import com.planwith.planwith_fo_story.composition.application.query.StoryDetailScreenView.MemberScreenView;
import com.planwith.planwith_fo_story.composition.application.query.StoryDetailScreenView.MembershipScreenView;
import com.planwith.planwith_fo_story.composition.application.query.StoryDetailScreenView.ScheduleScreenView;
import com.planwith.planwith_fo_story.composition.domain.CommentUiPolicy;
import com.planwith.planwith_fo_story.composition.domain.CommentUiState;
import com.planwith.planwith_fo_story.domain.model.projection.MembershipEntitlementProjection;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class StoryDetailScreenComposer implements StoryDetailScreenUseCase {

	private final StoryQueryUseCase storyQueryUseCase;
	private final MemberBioQueryPort memberBioQueryPort;
	private final FollowSummaryQueryPort followSummaryQueryPort;
	private final ScheduleDetailQueryPort scheduleDetailQueryPort;
	private final StoryLikeStatusQueryPort storyLikeStatusQueryPort;
	private final StoryCommentListQueryPort storyCommentListQueryPort;
	private final MembershipEntitlementProjectionPort membershipEntitlementProjectionPort;

	public StoryDetailScreenComposer(
			StoryQueryUseCase storyQueryUseCase,
			MemberBioQueryPort memberBioQueryPort,
			FollowSummaryQueryPort followSummaryQueryPort,
			ScheduleDetailQueryPort scheduleDetailQueryPort,
			StoryLikeStatusQueryPort storyLikeStatusQueryPort,
			StoryCommentListQueryPort storyCommentListQueryPort,
			MembershipEntitlementProjectionPort membershipEntitlementProjectionPort
	) {
		this.storyQueryUseCase = storyQueryUseCase;
		this.memberBioQueryPort = memberBioQueryPort;
		this.followSummaryQueryPort = followSummaryQueryPort;
		this.scheduleDetailQueryPort = scheduleDetailQueryPort;
		this.storyLikeStatusQueryPort = storyLikeStatusQueryPort;
		this.storyCommentListQueryPort = storyCommentListQueryPort;
		this.membershipEntitlementProjectionPort = membershipEntitlementProjectionPort;
	}

	@Override
	public StoryDetailScreenView compose(GetStoryDetailScreenQuery query) {
		log.info("StoryDetailScreenComposer : compose : 스토리 상세 화면 조합 시작 - storyUuid={}", query.storyUuid());
		StoryDetailView story = storyQueryUseCase.getDetail(new GetStoryDetailQuery(query.storyUuid(), query.viewerUuid()));
		UUID authorUuid = UUID.fromString(story.memberUuid());

		MemberScreenView member = composeMemberSection(story.author(), authorUuid);
		FollowScreenView follow = followSummaryQueryPort.findByMemberUuid(authorUuid);
		ScheduleScreenView schedule = composeScheduleSection(story.scheduleUuid());
		LikeScreenView like = composeLikeSection(story, query.viewerUuid());
		CommentScreenView comment = composeCommentSection(story, query.viewerUuid());
		MembershipScreenView membership = composeMembershipSection(query.viewerUuid(), authorUuid);

		log.info("StoryDetailScreenComposer : compose : 스토리 상세 화면 조합 완료 - storyUuid={}", query.storyUuid());
		return new StoryDetailScreenView(story, member, follow, schedule, like, comment, membership);
	}

	private MemberScreenView composeMemberSection(StoryAuthorView author, UUID authorUuid) {
		String bio = memberBioQueryPort.findBioByMemberUuid(authorUuid).orElse(null);
		return new MemberScreenView(
				author.memberUuid(),
				author.nickname(),
				author.profileImage(),
				bio
		);
	}

	private ScheduleScreenView composeScheduleSection(String scheduleUuid) {
		if (scheduleUuid == null) {
			return null;
		}
		return scheduleDetailQueryPort.findByScheduleUuid(UUID.fromString(scheduleUuid)).orElse(null);
	}

	private LikeScreenView composeLikeSection(StoryDetailView story, UUID viewerUuid) {
		boolean liked = viewerUuid != null
				&& storyLikeStatusQueryPort.isLikedByViewer(UUID.fromString(story.storyUuid()), viewerUuid);
		return new LikeScreenView(liked, story.storyLikeCount());
	}

	private CommentScreenView composeCommentSection(StoryDetailView story, UUID viewerUuid) {
		CommentUiState uiState = CommentUiPolicy.resolve(story.commentEnabled(), viewerUuid);
		return switch (uiState) {
			case DISABLED -> new CommentScreenView(
					CommentUiState.DISABLED,
					CommentUiPolicy.DISABLED_MESSAGE,
					List.of()
			);
			case LOGIN_REQUIRED -> new CommentScreenView(
					CommentUiState.LOGIN_REQUIRED,
					null,
					List.of()
			);
			case COMMENT_UI -> new CommentScreenView(
					CommentUiState.COMMENT_UI,
					null,
					fetchComments(story.storyUuid())
			);
		};
	}

	private List<StoryCommentItemView> fetchComments(String storyUuid) {
		List<StoryCommentItemView> comments = storyCommentListQueryPort.findByStoryUuid(UUID.fromString(storyUuid));
		return comments == null ? Collections.emptyList() : comments;
	}

	private MembershipScreenView composeMembershipSection(UUID viewerUuid, UUID authorUuid) {
		if (viewerUuid == null) {
			return new MembershipScreenView(false);
		}
		boolean subscribed = membershipEntitlementProjectionPort.findByMemberAndCreator(viewerUuid, authorUuid)
				.map(MembershipEntitlementProjection::canViewMembershipStories)
				.orElse(false);
		return new MembershipScreenView(subscribed);
	}
}
