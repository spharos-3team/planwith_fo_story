package com.planwith.planwith_fo_story.composition.adapter.out.schedule;

import java.util.Optional;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_story.composition.application.port.out.ScheduleDetailQueryPort;
import com.planwith.planwith_fo_story.composition.application.query.StoryDetailScreenView.ScheduleScreenView;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(
		prefix = "story.detail-screen.schedule",
		name = "query-enabled",
		havingValue = "false",
		matchIfMissing = true
)
public class DisabledScheduleDetailQueryAdapter implements ScheduleDetailQueryPort {

	@Override
	public Optional<ScheduleScreenView> findByScheduleUuid(UUID scheduleUuid) {
		log.debug(
				"DisabledScheduleDetailQueryAdapter : findByScheduleUuid : Schedule Service 미연동 상태 - scheduleUuid={}",
				scheduleUuid
		);
		return Optional.empty();
	}
}
