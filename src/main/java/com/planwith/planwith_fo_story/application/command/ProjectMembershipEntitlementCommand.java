package com.planwith.planwith_fo_story.application.command;

import java.util.UUID;

public record ProjectMembershipEntitlementCommand(
		UUID memberUuid,
		UUID creatorUuid,
		UUID membershipUuid,
		long sourceVersion
) {
}
