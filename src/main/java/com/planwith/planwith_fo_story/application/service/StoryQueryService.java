package com.planwith.planwith_fo_story.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_story.application.port.in.StoryQueryUseCase;
import com.planwith.planwith_fo_story.application.port.out.MemberProfileProjectionPort;
import com.planwith.planwith_fo_story.application.port.out.MembershipEntitlementProjectionPort;
import com.planwith.planwith_fo_story.application.port.out.StoryQueryCachePort;
import com.planwith.planwith_fo_story.application.port.out.StoryQueryPort;
import com.planwith.planwith_fo_story.application.query.GetStoryDetailQuery;
import com.planwith.planwith_fo_story.application.query.GetStoryFeedQuery;
import com.planwith.planwith_fo_story.application.query.GetStoryListQuery;
import com.planwith.planwith_fo_story.application.query.StoryDetailView;
import com.planwith.planwith_fo_story.application.query.StoryFeedView;
import com.planwith.planwith_fo_story.application.query.StoryListView;
import com.planwith.planwith_fo_story.application.query.StorySummaryView;
import com.planwith.planwith_fo_story.domain.exception.StoryAccessDeniedException;
import com.planwith.planwith_fo_story.domain.exception.StoryNotFoundException;
import com.planwith.planwith_fo_story.domain.model.Story;
import com.planwith.planwith_fo_story.domain.model.projection.MembershipEntitlementProjection;
import com.planwith.planwith_fo_story.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_story.domain.service.StoryAccessPolicy;
import com.planwith.planwith_fo_story.domain.service.StorySchedulePolicy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
public class StoryQueryService implements StoryQueryUseCase {

	private final StoryQueryPort storyQueryPort;
	private final StoryQueryCachePort storyQueryCachePort;
	private final MemberProfileProjectionPort memberProfileProjectionPort;
	private final MembershipEntitlementProjectionPort membershipEntitlementProjectionPort;
	private final StoryAccessPolicy accessPolicy;
	private final StorySchedulePolicy schedulePolicy;

	public StoryQueryService(
			StoryQueryPort storyQueryPort,
			StoryQueryCachePort storyQueryCachePort,
			MemberProfileProjectionPort memberProfileProjectionPort,
			MembershipEntitlementProjectionPort membershipEntitlementProjectionPort
	) {
		this.storyQueryPort = storyQueryPort;
		this.storyQueryCachePort = storyQueryCachePort;
		this.memberProfileProjectionPort = memberProfileProjectionPort;
		this.membershipEntitlementProjectionPort = membershipEntitlementProjectionPort;
		this.accessPolicy = new StoryAccessPolicy();
		this.schedulePolicy = new StorySchedulePolicy();
	}

	@Override
	public StoryDetailView getDetail(GetStoryDetailQuery query) {
		log.debug("StoryQueryService : getDetail : 스토리 상세 조회 시작 - storyUuid={}", query.storyUuid());
		StoryDetailView view = storyQueryCachePort.findDetail(query.storyUuid())
				.map(cached -> requireReadable(cached, query.viewerUuid(), query.storyUuid()))
				.orElseGet(() -> loadDetailFromDb(query));
		return maskScheduleForViewer(view, query.viewerUuid());
	}

	@Override
	public StoryListView getList(GetStoryListQuery query) {
		log.debug("StoryQueryService : getList : 스토리 목록 조회 시작 - authorUuid={}", query.authorUuid());
		List<StorySummaryView> items = storyQueryPort
				.findActiveByMemberUuid(query.authorUuid(), query.offset(), query.resolvedSize())
				.stream()
				.filter(story -> canRead(story, query.viewerUuid()))
				.map(this::toSummary)
				.toList();
		return new StoryListView(items, query.page(), query.resolvedSize());
	}

	@Override
	public StoryFeedView getFeed(GetStoryFeedQuery query) {
		log.debug("StoryQueryService : getFeed : 스토리 피드 조회 시작");
		if (query.viewerUuid() != null) {
			return storyQueryCachePort.findFeed(query.viewerUuid())
					.orElseGet(() -> loadFeedFromDb(query));
		}
		return loadFeedFromDb(query);
	}

	private StoryDetailView loadDetailFromDb(GetStoryDetailQuery query) {
		Story story = storyQueryPort.findActiveByStoryUuid(query.storyUuid())
				.orElseThrow(() -> new StoryNotFoundException(query.storyUuid().toString()));
		if (!canRead(story, query.viewerUuid())) {
			log.info("StoryQueryService : getDetail : 스토리 조회 권한 없음 - storyUuid={}", query.storyUuid());
			throw new StoryAccessDeniedException();
		}
		StoryDetailView view = toDetail(story);
		storyQueryCachePort.saveDetail(query.storyUuid(), view);
		return view;
	}

	private StoryFeedView loadFeedFromDb(GetStoryFeedQuery query) {
		List<StorySummaryView> items = storyQueryPort
				.findRecentActive(query.offset(), query.resolvedSize())
				.stream()
				.filter(story -> canRead(story, query.viewerUuid()))
				.map(this::toSummary)
				.toList();
		StoryFeedView view = new StoryFeedView(items, query.page(), query.resolvedSize());
		if (query.viewerUuid() != null) {
			storyQueryCachePort.saveFeed(query.viewerUuid(), view);
		}
		return view;
	}

	private StoryDetailView requireReadable(StoryDetailView view, UUID viewerUuid, UUID storyUuid) {
		if (canRead(view, viewerUuid)) {
			return view;
		}
		log.info("StoryQueryService : getDetail : 스토리 조회 권한 없음 - storyUuid={}", storyUuid);
		throw new StoryAccessDeniedException();
	}

	private boolean canRead(Story story, UUID viewerUuid) {
		return accessPolicy.canRead(
				story,
				toViewer(viewerUuid),
				hasMembershipEntitlement(viewerUuid, story.memberUuid().value())
		);
	}

	private boolean canRead(StoryDetailView view, UUID viewerUuid) {
		return accessPolicy.canRead(
				MemberUuid.of(view.memberUuid()),
				view.visibilityScope(),
				visibilityMembersOf(view),
				toViewer(viewerUuid),
				hasMembershipEntitlement(viewerUuid, UUID.fromString(view.memberUuid()))
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

	private static List<MemberUuid> visibilityMembersOf(StoryDetailView view) {
		if (view.visibilityMemberUuids() == null) {
			return List.of();
		}
		return view.visibilityMemberUuids().stream().map(MemberUuid::of).toList();
	}

	private StoryDetailView maskScheduleForViewer(StoryDetailView view, UUID viewerUuid) {
		MemberUuid author = MemberUuid.of(view.memberUuid());
		MemberUuid viewer = viewerUuid == null ? null : MemberUuid.of(viewerUuid);
		UUID scheduleUuid = view.scheduleUuid() == null ? null : UUID.fromString(view.scheduleUuid());
		if (schedulePolicy.canExposeScheduleReference(author, viewer, scheduleUuid, view.scheduleVisible())) {
			return view;
		}
		log.debug("StoryQueryService : maskScheduleForViewer : 일정 UUID를 조회자에게 숨김 - storyUuid={}", view.storyUuid());
		return view.hideScheduleReference();
	}

	private StoryDetailView toDetail(Story story) {
		return StoryViewMapper.toDetail(
				story,
				memberProfileProjectionPort.findByMemberUuid(story.memberUuid().value()).orElse(null)
		);
	}

	private StorySummaryView toSummary(Story story) {
		return StoryViewMapper.toSummary(
				story,
				memberProfileProjectionPort.findByMemberUuid(story.memberUuid().value()).orElse(null)
		);
	}
}
