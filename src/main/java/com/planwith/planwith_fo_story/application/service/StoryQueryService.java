package com.planwith.planwith_fo_story.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_story.application.port.in.StoryQueryUseCase;
import com.planwith.planwith_fo_story.application.port.out.MemberProfileProjectionPort;
import com.planwith.planwith_fo_story.application.port.out.MembershipEntitlementProjectionPort;
import com.planwith.planwith_fo_story.application.port.out.StoryFeedMemberQueryPort;
import com.planwith.planwith_fo_story.application.port.out.StoryFeedMembershipQueryPort;
import com.planwith.planwith_fo_story.application.port.out.StoryNicknameSearchPort;
import com.planwith.planwith_fo_story.application.port.out.StoryQueryCachePort;
import com.planwith.planwith_fo_story.application.port.out.StoryQueryPort;
import com.planwith.planwith_fo_story.application.query.GetStoryDetailQuery;
import com.planwith.planwith_fo_story.application.query.GetStoryFeedQuery;
import com.planwith.planwith_fo_story.application.query.GetStoryListQuery;
import com.planwith.planwith_fo_story.application.query.SearchStoryQuery;
import com.planwith.planwith_fo_story.application.query.StoryDetailView;
import com.planwith.planwith_fo_story.application.query.StoryFeedType;
import com.planwith.planwith_fo_story.application.query.StoryFeedView;
import com.planwith.planwith_fo_story.application.query.StoryListView;
import com.planwith.planwith_fo_story.application.query.StorySearchType;
import com.planwith.planwith_fo_story.application.query.StorySortType;
import com.planwith.planwith_fo_story.application.query.StorySummaryView;
import com.planwith.planwith_fo_story.domain.exception.StoryAccessDeniedException;
import com.planwith.planwith_fo_story.domain.exception.StoryNotFoundException;
import com.planwith.planwith_fo_story.domain.model.Story;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;
import com.planwith.planwith_fo_story.domain.model.projection.MembershipEntitlementProjection;
import com.planwith.planwith_fo_story.domain.model.projection.MemberProfileProjection;
import com.planwith.planwith_fo_story.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_story.domain.service.StoryAccessPolicy;
import com.planwith.planwith_fo_story.domain.service.StorySchedulePolicy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
public class StoryQueryService implements StoryQueryUseCase {

	private static final int QUERY_BATCH_SIZE = 100;

	private final StoryQueryPort storyQueryPort;
	private final StoryQueryCachePort storyQueryCachePort;
	private final MemberProfileProjectionPort memberProfileProjectionPort;
	private final MembershipEntitlementProjectionPort membershipEntitlementProjectionPort;
	private final StoryFeedMemberQueryPort storyFeedMemberQueryPort;
	private final StoryFeedMembershipQueryPort storyFeedMembershipQueryPort;
	private final StoryNicknameSearchPort storyNicknameSearchPort;
	private final StoryAccessPolicy accessPolicy;
	private final StorySchedulePolicy schedulePolicy;

	public StoryQueryService(
			StoryQueryPort storyQueryPort,
			StoryQueryCachePort storyQueryCachePort,
			MemberProfileProjectionPort memberProfileProjectionPort,
			MembershipEntitlementProjectionPort membershipEntitlementProjectionPort,
			StoryFeedMemberQueryPort storyFeedMemberQueryPort,
			StoryFeedMembershipQueryPort storyFeedMembershipQueryPort,
			StoryNicknameSearchPort storyNicknameSearchPort
	) {
		this.storyQueryPort = storyQueryPort;
		this.storyQueryCachePort = storyQueryCachePort;
		this.memberProfileProjectionPort = memberProfileProjectionPort;
		this.membershipEntitlementProjectionPort = membershipEntitlementProjectionPort;
		this.storyFeedMemberQueryPort = storyFeedMemberQueryPort;
		this.storyFeedMembershipQueryPort = storyFeedMembershipQueryPort;
		this.storyNicknameSearchPort = storyNicknameSearchPort;
		this.accessPolicy = new StoryAccessPolicy();
		this.schedulePolicy = new StorySchedulePolicy();
	}

	@Override
	public StoryDetailView getDetail(GetStoryDetailQuery query) {
		Story story = storyQueryPort.findByStoryUuid(query.storyUuid())
				.orElseThrow(() -> new StoryNotFoundException(query.storyUuid().toString()));
		if (story.isDeleted()) {
			throw new StoryNotFoundException(query.storyUuid().toString());
		}
		if (!canRead(story, query.viewerUuid())) {
			throw new StoryAccessDeniedException();
		}
		StoryDetailView view = toDetail(story);
		storyQueryCachePort.saveDetail(query.storyUuid(), view);
		return maskScheduleForViewer(view, query.viewerUuid());
	}

	@Override
	public StoryListView getList(GetStoryListQuery query) {
		Set<UUID> authors = query.authorUuid() == null ? null : Set.of(query.authorUuid());
		List<Story> stories = findReadableStories(
				authors,
				query.sort(),
				query.viewerUuid(),
				query.offset(),
				query.resolvedSize(),
				story -> true
		);
		List<StorySummaryView> items = toSummaries(stories);
		return new StoryListView(items, Math.max(0, query.page()), query.resolvedSize());
	}

	@Override
	public StoryFeedView getFeed(GetStoryFeedQuery query) {
		Set<UUID> authors = resolveFeedAuthors(query.feedType(), query.viewerUuid());
		Predicate<Story> feedCondition = query.feedType() == StoryFeedType.MEMBERSHIP
				? story -> story.visibilityScope() == VisibilityScope.MEMBERSHIP
				: story -> true;
		List<Story> stories = findReadableStories(
				authors,
				query.sort(),
				query.viewerUuid(),
				query.offset(),
				query.resolvedSize(),
				feedCondition
		);
		List<StorySummaryView> items = toSummaries(stories);
		return new StoryFeedView(items, Math.max(0, query.page()), query.resolvedSize());
	}

	@Override
	public StoryListView search(SearchStoryQuery query) {
		Set<UUID> authors = query.type() == StorySearchType.NICKNAME
				? storyNicknameSearchPort.findMemberUuidsByNickname(query.keyword())
				: null;
		if (authors != null && authors.isEmpty()) {
			return new StoryListView(List.of(), Math.max(0, query.page()), query.resolvedSize());
		}
		List<Story> stories = findReadableStories(
				query.offset(),
				query.resolvedSize(),
				story -> true,
				(queryOffset, batchSize) -> query.type() == StorySearchType.NICKNAME
						? storyQueryPort.findActive(authors, StorySortType.LATEST, queryOffset, batchSize)
						: storyQueryPort.searchActive(query.type(), query.keyword(), queryOffset, batchSize),
				query.viewerUuid()
		);
		return new StoryListView(toSummaries(stories), Math.max(0, query.page()), query.resolvedSize());
	}

	private Set<UUID> resolveFeedAuthors(StoryFeedType feedType, UUID viewerUuid) {
		if (feedType == StoryFeedType.FOLLOWING) {
			return storyFeedMemberQueryPort.findEligibleFollowingAuthors(viewerUuid).orElse(null);
		}
		Set<UUID> joinedCreators = storyFeedMembershipQueryPort.findJoinedCreatorUuids(viewerUuid);
		if (joinedCreators.isEmpty()) {
			return Set.of();
		}
		return storyFeedMemberQueryPort.filterEligibleAuthors(joinedCreators).orElse(Set.of());
	}

	private List<Story> findReadableStories(
			Set<UUID> authors,
			StorySortType sort,
			UUID viewerUuid,
			int offset,
			int size,
			Predicate<Story> feedCondition
	) {
		if (authors != null && authors.isEmpty()) {
			return List.of();
		}
		return findReadableStories(
				offset,
				size,
				feedCondition,
				(queryOffset, batchSize) -> storyQueryPort.findActive(authors, sort, queryOffset, batchSize),
				viewerUuid
		);
	}

	private List<Story> findReadableStories(
			int offset,
			int size,
			Predicate<Story> condition,
			StoryPageLoader pageLoader,
			UUID viewerUuid
	) {
		int required = offset + size;
		int queryOffset = 0;
		List<Story> readable = new ArrayList<>(required);
		while (readable.size() < required) {
			List<Story> candidates = pageLoader.load(queryOffset, QUERY_BATCH_SIZE);
			for (Story story : candidates) {
				if (condition.test(story) && canRead(story, viewerUuid)) {
					readable.add(story);
				}
			}
			if (candidates.size() < QUERY_BATCH_SIZE) {
				break;
			}
			queryOffset += QUERY_BATCH_SIZE;
		}
		if (offset >= readable.size()) {
			return List.of();
		}
		return List.copyOf(readable.subList(offset, Math.min(required, readable.size())));
	}

	@FunctionalInterface
	private interface StoryPageLoader {

		List<Story> load(int offset, int size);
	}

	private boolean canRead(Story story, UUID viewerUuid) {
		boolean membershipEntitled = story.visibilityScope() == VisibilityScope.MEMBERSHIP
				&& hasMembershipEntitlement(viewerUuid, story.memberUuid().value());
		return accessPolicy.canRead(
				story,
				toViewer(viewerUuid),
				membershipEntitled
		);
	}

	private boolean hasMembershipEntitlement(UUID viewerUuid, UUID authorUuid) {
		if (viewerUuid == null) {
			return false;
		}
		return membershipEntitlementProjectionPort.findByMemberAndCreator(viewerUuid, authorUuid)
				.map(MembershipEntitlementProjection::canViewMembershipStories)
				.orElse(false);
	}

	private static MemberUuid toViewer(UUID viewerUuid) {
		return viewerUuid == null ? null : MemberUuid.of(viewerUuid);
	}

	private StoryDetailView maskScheduleForViewer(StoryDetailView view, UUID viewerUuid) {
		MemberUuid author = MemberUuid.of(view.memberUuid());
		MemberUuid viewer = viewerUuid == null ? null : MemberUuid.of(viewerUuid);
		UUID scheduleUuid = view.scheduleUuid() == null ? null : UUID.fromString(view.scheduleUuid());
		if (schedulePolicy.canExposeScheduleReference(author, viewer, scheduleUuid, view.scheduleVisible())) {
			return view;
		}
		return view.hideScheduleReference();
	}

	private StoryDetailView toDetail(Story story) {
		return StoryViewMapper.toDetail(
				story,
				memberProfileProjectionPort.findByMemberUuid(story.memberUuid().value()).orElse(null)
		);
	}

	private List<StorySummaryView> toSummaries(List<Story> stories) {
		Set<UUID> authorUuids = stories.stream()
				.map(story -> story.memberUuid().value())
				.collect(java.util.stream.Collectors.toUnmodifiableSet());
		Map<UUID, MemberProfileProjection> authors = memberProfileProjectionPort.findByMemberUuids(authorUuids);
		return stories.stream()
				.map(story -> StoryViewMapper.toSummary(story, authors.get(story.memberUuid().value())))
				.toList();
	}
}
