package com.planwith.planwith_fo_story.composition.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.planwith.planwith_fo_story.application.port.in.StoryQueryUseCase;
import com.planwith.planwith_fo_story.application.port.out.MembershipEntitlementProjectionPort;
import com.planwith.planwith_fo_story.application.query.StoryAuthorView;
import com.planwith.planwith_fo_story.application.query.StoryDetailView;
import com.planwith.planwith_fo_story.composition.application.port.out.FollowSummaryQueryPort;
import com.planwith.planwith_fo_story.composition.application.port.out.MemberBioQueryPort;
import com.planwith.planwith_fo_story.composition.application.port.out.ScheduleDetailQueryPort;
import com.planwith.planwith_fo_story.composition.application.port.out.StoryCommentListQueryPort;
import com.planwith.planwith_fo_story.composition.application.port.out.StoryLikeStatusQueryPort;
import com.planwith.planwith_fo_story.composition.application.query.GetStoryDetailScreenQuery;
import com.planwith.planwith_fo_story.composition.application.query.StoryCommentItemView;
import com.planwith.planwith_fo_story.composition.application.query.StoryDetailScreenView;
import com.planwith.planwith_fo_story.composition.application.query.StoryDetailScreenView.FollowScreenView;
import com.planwith.planwith_fo_story.composition.domain.CommentUiPolicy;
import com.planwith.planwith_fo_story.composition.domain.CommentUiState;
import com.planwith.planwith_fo_story.domain.model.AiModerationStatus;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;

@ExtendWith(MockitoExtension.class)
class StoryDetailScreenComposerTest {

	@Mock
	private StoryQueryUseCase storyQueryUseCase;
	@Mock
	private MemberBioQueryPort memberBioQueryPort;
	@Mock
	private FollowSummaryQueryPort followSummaryQueryPort;
	@Mock
	private ScheduleDetailQueryPort scheduleDetailQueryPort;
	@Mock
	private StoryLikeStatusQueryPort storyLikeStatusQueryPort;
	@Mock
	private StoryCommentListQueryPort storyCommentListQueryPort;
	@Mock
	private MembershipEntitlementProjectionPort membershipEntitlementProjectionPort;

	private StoryDetailScreenComposer composer;

	@BeforeEach
	void setUp() {
		composer = new StoryDetailScreenComposer(
				storyQueryUseCase,
				memberBioQueryPort,
				followSummaryQueryPort,
				scheduleDetailQueryPort,
				storyLikeStatusQueryPort,
				storyCommentListQueryPort,
				membershipEntitlementProjectionPort
		);
	}

	@Test
	void composeBuildsDisabledCommentSectionWhenCommentDisabled() {
		UUID storyUuid = UUID.randomUUID();
		UUID authorUuid = UUID.randomUUID();
		StoryDetailView story = sampleStory(storyUuid, authorUuid, false);

		when(storyQueryUseCase.getDetail(any())).thenReturn(story);
		when(memberBioQueryPort.findBioByMemberUuid(authorUuid)).thenReturn(Optional.of("소개글"));
		when(followSummaryQueryPort.findByMemberUuid(authorUuid)).thenReturn(new FollowScreenView(3L, 2L));

		StoryDetailScreenView result = composer.compose(new GetStoryDetailScreenQuery(storyUuid, UUID.randomUUID()));

		assertThat(result.comment().uiState()).isEqualTo(CommentUiState.DISABLED);
		assertThat(result.comment().message()).isEqualTo(CommentUiPolicy.DISABLED_MESSAGE);
		assertThat(result.comment().items()).isEmpty();
		assertThat(result.member().bio()).isEqualTo("소개글");
		assertThat(result.follow().followerCount()).isEqualTo(3L);
	}

	@Test
	void composeBuildsLoginRequiredCommentSectionForGuest() {
		UUID storyUuid = UUID.randomUUID();
		UUID authorUuid = UUID.randomUUID();
		StoryDetailView story = sampleStory(storyUuid, authorUuid, true);

		when(storyQueryUseCase.getDetail(any())).thenReturn(story);
		when(memberBioQueryPort.findBioByMemberUuid(authorUuid)).thenReturn(Optional.empty());
		when(followSummaryQueryPort.findByMemberUuid(authorUuid)).thenReturn(new FollowScreenView(0L, 0L));

		StoryDetailScreenView result = composer.compose(new GetStoryDetailScreenQuery(storyUuid, null));

		assertThat(result.comment().uiState()).isEqualTo(CommentUiState.LOGIN_REQUIRED);
		assertThat(result.comment().message()).isNull();
		assertThat(result.membership().subscribed()).isFalse();
	}

	@Test
	void composeLoadsCommentsWhenCommentEnabledAndLoggedIn() {
		UUID storyUuid = UUID.randomUUID();
		UUID authorUuid = UUID.randomUUID();
		UUID viewerUuid = UUID.randomUUID();
		StoryDetailView story = sampleStory(storyUuid, authorUuid, true);
		StoryCommentItemView comment = new StoryCommentItemView(
				UUID.randomUUID().toString(),
				viewerUuid.toString(),
				"좋은 글이에요",
				java.time.LocalDateTime.of(2026, 8, 1, 10, 0)
		);

		when(storyQueryUseCase.getDetail(any())).thenReturn(story);
		when(memberBioQueryPort.findBioByMemberUuid(authorUuid)).thenReturn(Optional.empty());
		when(followSummaryQueryPort.findByMemberUuid(authorUuid)).thenReturn(new FollowScreenView(0L, 0L));
		when(storyCommentListQueryPort.findByStoryUuid(storyUuid)).thenReturn(List.of(comment));
		when(storyLikeStatusQueryPort.isLikedByViewer(storyUuid, viewerUuid)).thenReturn(true);
		when(membershipEntitlementProjectionPort.findByMemberAndCreator(viewerUuid, authorUuid))
				.thenReturn(Optional.empty());

		StoryDetailScreenView result = composer.compose(new GetStoryDetailScreenQuery(storyUuid, viewerUuid));

		assertThat(result.comment().uiState()).isEqualTo(CommentUiState.COMMENT_UI);
		assertThat(result.comment().items()).hasSize(1);
		assertThat(result.like().liked()).isTrue();
	}

	private static StoryDetailView sampleStory(UUID storyUuid, UUID authorUuid, boolean commentEnabled) {
		return new StoryDetailView(
				storyUuid.toString(),
				authorUuid.toString(),
				null,
				false,
				"제목",
				"내용",
				"https://img.example/cover.png",
				LocalDate.of(2026, 8, 1),
				LocalDate.of(2026, 8, 5),
				commentEnabled,
				VisibilityScope.ALL,
				AiModerationStatus.UNVERIFIED,
				0L,
				5L,
				2L,
				java.time.LocalDateTime.of(2026, 8, 1, 9, 0),
				java.time.LocalDateTime.of(2026, 8, 1, 9, 0),
				List.of(),
				List.of(),
				List.of(),
				List.of(),
				new StoryAuthorView(authorUuid.toString(), "작성자", "https://img.example/profile.png")
		);
	}
}
