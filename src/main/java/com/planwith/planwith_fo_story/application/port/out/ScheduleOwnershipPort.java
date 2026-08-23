package com.planwith.planwith_fo_story.application.port.out;

import java.util.UUID;

public interface ScheduleOwnershipPort {

	boolean isOwnedBy(UUID scheduleUuid, UUID memberUuid);
}
