package com.planwith.planwith_fo_story.domain.service;

import com.planwith.planwith_fo_story.domain.model.Story;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;
import com.planwith.planwith_fo_story.domain.model.vo.MemberUuid;

public class StoryVisibilityPolicy {

	public boolean canView(Story story, MemberUuid viewerUuid, boolean hasMembershipEntitlement) {
		if (story.isDeleted()) {
			return false;
		}
		if (viewerUuid != null && story.isOwnedBy(viewerUuid)) {
			return true;
		}
		VisibilityScope scope = story.visibilityScope();
		return switch (scope) {
			case ALL -> true;
			case PRIVATE -> false;
			case MEMBER -> viewerUuid != null;
			case MEMBERSHIP -> hasMembershipEntitlement;
		};
	}
}
