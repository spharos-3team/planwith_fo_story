package com.planwith.planwith_fo_story.adapter.out.persistence.projection;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_story.adapter.out.persistence.projection.StoryMembershipProjectionJpaEntity.StoryMembershipProjectionId;
import com.planwith.planwith_fo_story.application.port.out.MembershipEntitlementProjectionPort;
import com.planwith.planwith_fo_story.domain.model.projection.MembershipEntitlementProjection;
import com.planwith.planwith_fo_story.domain.model.vo.MemberUuid;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MembershipEntitlementProjectionAdapter implements MembershipEntitlementProjectionPort {

	private final SpringDataStoryMembershipProjectionRepository repository;

	@Override
	@Transactional
	public void save(MembershipEntitlementProjection projection) {
		StoryMembershipProjectionId id = new StoryMembershipProjectionId(
				projection.memberUuid().value(),
				projection.creatorUuid().value()
		);
		StoryMembershipProjectionJpaEntity entity = repository.findById(id)
				.orElseGet(() -> new StoryMembershipProjectionJpaEntity(
						projection.memberUuid().value(),
						projection.creatorUuid().value()
				));
		entity.apply(
				projection.membershipUuid(),
				projection.subscribed(),
				projection.sourceVersion(),
				projection.synchronizedAt()
		);
		repository.save(entity);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<MembershipEntitlementProjection> findByMemberAndCreator(UUID memberUuid, UUID creatorUuid) {
		return repository.findById(new StoryMembershipProjectionId(memberUuid, creatorUuid))
				.map(entity -> new MembershipEntitlementProjection(
						MemberUuid.of(entity.id().memberUuid()),
						MemberUuid.of(entity.id().creatorUuid()),
						entity.membershipUuid(),
						entity.subscribed(),
						entity.sourceVersion(),
						entity.synchronizedAt()
				));
	}
}
