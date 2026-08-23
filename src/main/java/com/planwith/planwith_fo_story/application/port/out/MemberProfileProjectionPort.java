package com.planwith.planwith_fo_story.application.port.out;

import java.util.Optional;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.planwith.planwith_fo_story.domain.model.projection.MemberProfileProjection;

public interface MemberProfileProjectionPort {

	void save(MemberProfileProjection projection);

	Optional<MemberProfileProjection> findByMemberUuid(UUID memberUuid);

	Map<UUID, MemberProfileProjection> findByMemberUuids(Set<UUID> memberUuids);
}
