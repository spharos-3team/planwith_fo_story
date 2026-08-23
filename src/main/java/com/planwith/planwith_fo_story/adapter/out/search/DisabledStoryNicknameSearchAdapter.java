package com.planwith.planwith_fo_story.adapter.out.search;

import java.util.Set;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_story.application.port.out.StoryNicknameSearchPort;

@Component
@ConditionalOnProperty(prefix = "story.search", name = "member-query-enabled", havingValue = "false", matchIfMissing = true)
public class DisabledStoryNicknameSearchAdapter implements StoryNicknameSearchPort {

	@Override
	public Set<UUID> findMemberUuidsByNickname(String nickname) {
		return Set.of();
	}
}
