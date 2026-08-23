package com.planwith.planwith_fo_story.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.planwith.planwith_fo_story.adapter.out.persistence.outbox.SpringDataStoryOutboxRepository;
import com.planwith.planwith_fo_story.adapter.out.persistence.story.SpringDataStoryRepository;
import com.planwith.planwith_fo_story.application.command.CreateStoryCommandFactory;
import com.planwith.planwith_fo_story.application.port.in.StoryCommandUseCase;
import com.planwith.planwith_fo_story.application.port.in.StoryQueryUseCase;
import com.planwith.planwith_fo_story.application.port.out.MembershipEntitlementProjectionPort;
import com.planwith.planwith_fo_story.application.port.out.StoryCounterPort;
import com.planwith.planwith_fo_story.application.port.out.StoryFeedMemberQueryPort;
import com.planwith.planwith_fo_story.application.port.out.StoryFeedMembershipQueryPort;
import com.planwith.planwith_fo_story.application.query.GetStoryFeedQuery;
import com.planwith.planwith_fo_story.application.query.GetStoryListQuery;
import com.planwith.planwith_fo_story.application.query.StoryDetailView;
import com.planwith.planwith_fo_story.application.query.StoryFeedType;
import com.planwith.planwith_fo_story.application.query.StorySortType;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;
import com.planwith.planwith_fo_story.domain.model.projection.MembershipEntitlementProjection;
import com.planwith.planwith_fo_story.domain.model.vo.MemberUuid;

@SpringBootTest
@ActiveProfiles("test")
class StoryFeedListQueryIntegrationTest {

	@Autowired
	private StoryCommandUseCase storyCommandUseCase;

	@Autowired
	private StoryQueryUseCase storyQueryUseCase;

	@Autowired
	private StoryCounterPort storyCounterPort;

	@Autowired
	private MembershipEntitlementProjectionPort membershipEntitlementProjectionPort;

	@Autowired
	private SpringDataStoryRepository storyRepository;

	@Autowired
	private SpringDataStoryOutboxRepository outboxRepository;

	@MockitoBean
	private StoryFeedMemberQueryPort storyFeedMemberQueryPort;

	@MockitoBean
	private StoryFeedMembershipQueryPort storyFeedMembershipQueryPort;

	@BeforeEach
	void setUp() {
		outboxRepository.deleteAll();
		storyRepository.deleteAll();
	}

	@Test
	void listsStoriesByViewAndLikeIndexesAndIncludesRegions() {
		StoryDetailView viewed = create(UUID.randomUUID(), "viewed", VisibilityScope.ALL);
		StoryDetailView liked = create(UUID.randomUUID(), "liked", VisibilityScope.ALL);
		UUID viewedUuid = UUID.fromString(viewed.storyUuid());
		UUID likedUuid = UUID.fromString(liked.storyUuid());
		storyCounterPort.incrementViewCount(viewedUuid);
		storyCounterPort.incrementViewCount(viewedUuid);
		storyCounterPort.changeLikeCount(likedUuid, 3L);

		var byView = storyQueryUseCase.getList(new GetStoryListQuery(null, null, 0, 20, StorySortType.VIEW));
		var byLike = storyQueryUseCase.getList(new GetStoryListQuery(null, null, 0, 20, StorySortType.LIKE));

		assertThat(byView.items()).first().extracting(item -> item.storyUuid()).isEqualTo(viewed.storyUuid());
		assertThat(byLike.items()).first().extracting(item -> item.storyUuid()).isEqualTo(liked.storyUuid());
		assertThat(byView.items().get(0).countryNames()).containsExactly("Korea");
		assertThat(byView.items().get(0).cityNames()).containsExactly("Seoul");
	}

	@Test
	void followingFeedUsesEligibleAuthorsAndAppliesVisibilityLast() {
		UUID viewerUuid = UUID.randomUUID();
		UUID followedAuthor = UUID.randomUUID();
		UUID otherAuthor = UUID.randomUUID();
		StoryDetailView visible = create(followedAuthor, "followed", VisibilityScope.ALL);
		create(otherAuthor, "other", VisibilityScope.ALL);
		when(storyFeedMemberQueryPort.findEligibleFollowingAuthors(viewerUuid))
				.thenReturn(Optional.of(Set.of(followedAuthor)));

		var feed = storyQueryUseCase.getFeed(new GetStoryFeedQuery(
				viewerUuid,
				0,
				20,
				StorySortType.LATEST,
				StoryFeedType.FOLLOWING
		));

		assertThat(feed.items()).extracting(item -> item.storyUuid()).containsExactly(visible.storyUuid());
	}

	@Test
	void membershipFeedIncludesOnlyEntitledMembershipStoriesFromEligibleCreators() {
		UUID viewerUuid = UUID.randomUUID();
		UUID creatorUuid = UUID.randomUUID();
		StoryDetailView membershipStory = create(creatorUuid, "membership", VisibilityScope.MEMBERSHIP);
		create(creatorUuid, "public", VisibilityScope.ALL);
		membershipEntitlementProjectionPort.save(new MembershipEntitlementProjection(
				MemberUuid.of(viewerUuid),
				MemberUuid.of(creatorUuid),
				UUID.randomUUID(),
				true,
				1L,
				LocalDateTime.of(2026, 8, 23, 23, 0)
		));
		when(storyFeedMembershipQueryPort.findJoinedCreatorUuids(viewerUuid)).thenReturn(Set.of(creatorUuid));
		when(storyFeedMemberQueryPort.filterEligibleAuthors(Set.of(creatorUuid)))
				.thenReturn(Optional.of(Set.of(creatorUuid)));

		var feed = storyQueryUseCase.getFeed(new GetStoryFeedQuery(
				viewerUuid,
				0,
				20,
				StorySortType.LATEST,
				StoryFeedType.MEMBERSHIP
		));

		assertThat(feed.items()).extracting(item -> item.storyUuid()).containsExactly(membershipStory.storyUuid());
	}

	private StoryDetailView create(UUID authorUuid, String title, VisibilityScope visibilityScope) {
		return storyCommandUseCase.create(CreateStoryCommandFactory.basic(
				authorUuid,
				title,
				"content",
				"https://img.example/cover.png",
				visibilityScope
		));
	}
}
