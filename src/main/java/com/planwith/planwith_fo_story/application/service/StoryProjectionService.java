package com.planwith.planwith_fo_story.application.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_story.application.command.ProjectCommentCountCommand;
import com.planwith.planwith_fo_story.application.command.ProjectLikeCountCommand;
import com.planwith.planwith_fo_story.application.command.ProjectMemberProfileCommand;
import com.planwith.planwith_fo_story.application.command.ProjectMembershipEntitlementCommand;
import com.planwith.planwith_fo_story.application.port.in.StoryProjectionUseCase;
import com.planwith.planwith_fo_story.application.port.out.MemberProfileProjectionPort;
import com.planwith.planwith_fo_story.application.port.out.MembershipEntitlementProjectionPort;
import com.planwith.planwith_fo_story.application.port.out.StoryCounterPort;
import com.planwith.planwith_fo_story.application.port.out.StoryQueryCachePort;
import com.planwith.planwith_fo_story.domain.model.projection.MemberProfileProjection;
import com.planwith.planwith_fo_story.domain.model.projection.MembershipEntitlementProjection;
import com.planwith.planwith_fo_story.domain.model.vo.MemberUuid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoryProjectionService implements StoryProjectionUseCase {

	private final MemberProfileProjectionPort memberProfileProjectionPort;
	private final StoryCounterPort storyCounterPort;
	private final MembershipEntitlementProjectionPort membershipEntitlementProjectionPort;
	private final StoryQueryCachePort storyQueryCachePort;
	private final Clock clock;

	@Override
	@Transactional
	public void projectMemberProfile(ProjectMemberProfileCommand command) {
		log.info("StoryProjectionService : projectMemberProfile : 회원 프로필 Projection 반영 시작 - memberUuid={}",
				command.memberUuid());
		MemberProfileProjection projection = new MemberProfileProjection(
				MemberUuid.of(command.memberUuid()),
				command.nickname(),
				command.profileImage(),
				command.memberStatus(),
				command.sourceVersion(),
				LocalDateTime.now(clock)
		);
		memberProfileProjectionPort.save(projection);
		storyQueryCachePort.evictFeed(command.memberUuid());
		log.info("StoryProjectionService : projectMemberProfile : 회원 프로필 Projection 반영 완료 - memberUuid={}",
				command.memberUuid());
	}

	@Override
	@Transactional
	public void projectLikeCreated(ProjectLikeCountCommand command) {
		if (!command.isStoryTarget()) {
			return;
		}
		log.info("StoryProjectionService : projectLikeCreated : 좋아요 수 Projection 증가 - storyUuid={}",
				command.targetUuid());
		applyCounterChange(command.targetUuid(), 1L, true);
	}

	@Override
	@Transactional
	public void projectLikeRemoved(ProjectLikeCountCommand command) {
		if (!command.isStoryTarget()) {
			return;
		}
		log.info("StoryProjectionService : projectLikeRemoved : 좋아요 수 Projection 감소 - storyUuid={}",
				command.targetUuid());
		applyCounterChange(command.targetUuid(), -1L, true);
	}

	@Override
	@Transactional
	public void projectCommentCreated(ProjectCommentCountCommand command) {
		if (!command.isStoryTarget()) {
			return;
		}
		applyCounterChange(command.targetUuid(), 1L, false);
	}

	@Override
	@Transactional
	public void projectCommentRemoved(ProjectCommentCountCommand command) {
		if (!command.isStoryTarget()) {
			return;
		}
		applyCounterChange(command.targetUuid(), -1L, false);
	}

	@Override
	@Transactional
	public void projectMembershipSubscribed(ProjectMembershipEntitlementCommand command) {
		log.info("StoryProjectionService : projectMembershipSubscribed : 멤버십 Projection 구독 반영 - memberUuid={}",
				command.memberUuid());
		saveMembership(command, true);
	}

	@Override
	@Transactional
	public void projectMembershipCanceled(ProjectMembershipEntitlementCommand command) {
		log.info("StoryProjectionService : projectMembershipCanceled : 멤버십 Projection 해지 반영 - memberUuid={}",
				command.memberUuid());
		saveMembership(command, false);
	}

	private void saveMembership(ProjectMembershipEntitlementCommand command, boolean subscribed) {
		membershipEntitlementProjectionPort.save(new MembershipEntitlementProjection(
				MemberUuid.of(command.memberUuid()),
				MemberUuid.of(command.creatorUuid()),
				command.membershipUuid(),
				subscribed,
				command.sourceVersion(),
				LocalDateTime.now(clock)
		));
		storyQueryCachePort.evictFeed(command.memberUuid());
	}

	private void applyCounterChange(UUID storyUuid, long delta, boolean likeCounter) {
		boolean updated = likeCounter
				? storyCounterPort.changeLikeCount(storyUuid, delta)
				: storyCounterPort.changeCommentCount(storyUuid, delta);
		if (!updated) {
			log.warn("StoryProjectionService : applyCounterChange : 대상 스토리가 없어 Counter Projection 생략 - storyUuid={}", storyUuid);
			return;
		}
		storyQueryCachePort.evictDetail(storyUuid);
		storyQueryCachePort.evictPopular();
	}
}
