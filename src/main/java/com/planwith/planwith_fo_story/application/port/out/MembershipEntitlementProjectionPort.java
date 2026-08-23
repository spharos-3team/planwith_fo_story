package com.planwith.planwith_fo_story.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_story.domain.model.projection.MembershipEntitlementProjection;

public interface MembershipEntitlementProjectionPort {

	void save(MembershipEntitlementProjection projection);

	Optional<MembershipEntitlementProjection> findByMemberAndCreator(UUID memberUuid, UUID creatorUuid);
}
