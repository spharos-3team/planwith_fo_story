package com.planwith.planwith_fo_story.composition.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface MemberBioQueryPort {

	Optional<String> findBioByMemberUuid(UUID memberUuid);
}
