package com.planwith.planwith_fo_story.adapter.out.schedule;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_story.application.port.out.ScheduleOwnershipPort;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(
		prefix = "story.schedule",
		name = "ownership-check-enabled",
		havingValue = "false",
		matchIfMissing = true
)
public class UuidReferenceScheduleOwnershipAdapter implements ScheduleOwnershipPort {

	@Override
	public boolean isOwnedBy(UUID scheduleUuid, UUID memberUuid) {
		log.debug(
				"UuidReferenceScheduleOwnershipAdapter : isOwnedBy : Schedule Service 미연동 상태에서 UUID Reference만 확인 - scheduleUuid={}",
				scheduleUuid
		);
		return scheduleUuid != null && memberUuid != null;
	}
}
