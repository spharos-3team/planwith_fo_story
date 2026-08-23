package com.planwith.planwith_fo_story.application.command;

import java.util.UUID;

import com.planwith.planwith_fo_story.domain.model.MemberStatus;

public record ProjectMemberProfileCommand(
		UUID memberUuid,
		String nickname,
		String profileImage,
		MemberStatus memberStatus,
		long sourceVersion
) {
}
