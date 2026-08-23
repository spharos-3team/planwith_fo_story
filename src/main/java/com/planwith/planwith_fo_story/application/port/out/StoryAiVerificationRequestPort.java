package com.planwith.planwith_fo_story.application.port.out;

import java.util.UUID;

public interface StoryAiVerificationRequestPort {

	void requestVerification(UUID storyUuid, UUID memberUuid);
}
