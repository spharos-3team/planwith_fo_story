package com.planwith.planwith_fo_story.domain.service;

import java.util.UUID;

import com.planwith.planwith_fo_story.domain.model.vo.MemberUuid;

public class StorySchedulePolicy {

	public boolean canExposeScheduleReference(
			MemberUuid authorUuid,
			MemberUuid viewerUuid,
			UUID scheduleUuid,
			boolean scheduleVisible
	) {
		if (scheduleUuid == null) {
			return false;
		}
		if (viewerUuid != null && authorUuid.equals(viewerUuid)) {
			return true;
		}
		return scheduleVisible;
	}
}
