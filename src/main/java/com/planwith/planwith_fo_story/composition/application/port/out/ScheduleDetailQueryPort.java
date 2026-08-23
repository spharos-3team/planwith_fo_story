package com.planwith.planwith_fo_story.composition.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_story.composition.application.query.StoryDetailScreenView.ScheduleScreenView;

public interface ScheduleDetailQueryPort {

	Optional<ScheduleScreenView> findByScheduleUuid(UUID scheduleUuid);
}
