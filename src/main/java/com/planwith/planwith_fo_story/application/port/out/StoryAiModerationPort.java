package com.planwith.planwith_fo_story.application.port.out;

import java.util.Optional;

public interface StoryAiModerationPort {

	Optional<StoryAiModerationResult> moderate(String title, String content);
}
