package com.planwith.planwith_fo_story.domain.service;

import java.util.List;

import com.planwith.planwith_fo_story.domain.model.Story;
import com.planwith.planwith_fo_story.domain.model.StoryVisibilityMember;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;
import com.planwith.planwith_fo_story.domain.model.vo.MemberUuid;

public class StoryAccessPolicy {

	public boolean canRead(Story story, MemberUuid viewerUuid, boolean membershipEntitled) {
		return canRead(
				story.isDeleted(),
				story.memberUuid(),
				story.visibilityScope(),
				story.visibilityMembers().stream().map(StoryVisibilityMember::memberUuid).toList(),
				viewerUuid,
				membershipEntitled
		);
	}

	public boolean canRead(
			MemberUuid authorUuid,
			VisibilityScope visibilityScope,
			List<MemberUuid> visibilityMemberUuids,
			MemberUuid viewerUuid,
			boolean membershipEntitled
	) {
		return canRead(false, authorUuid, visibilityScope, visibilityMemberUuids, viewerUuid, membershipEntitled);
	}

	private boolean canRead(
			boolean deleted,
			MemberUuid authorUuid,
			VisibilityScope visibilityScope,
			List<MemberUuid> visibilityMemberUuids,
			MemberUuid viewerUuid,
			boolean membershipEntitled
	) {
		if (deleted) {
			return false;
		}
		if (viewerUuid != null && authorUuid.equals(viewerUuid)) {
			return true;
		}
		return switch (visibilityScope) {
			case ALL -> true;
			case MEMBER -> viewerUuid != null;
			case MEMBERSHIP -> viewerUuid != null && membershipEntitled;
			case PRIVATE -> isDesignatedMember(visibilityMemberUuids, viewerUuid);
		};
	}

	private static boolean isDesignatedMember(List<MemberUuid> visibilityMemberUuids, MemberUuid viewerUuid) {
		if (viewerUuid == null || visibilityMemberUuids == null) {
			return false;
		}
		return visibilityMemberUuids.stream().anyMatch(viewerUuid::equals);
	}
}
