package com.planwith.planwith_fo_story.application.port.out;

import java.util.Set;
import java.util.UUID;

public interface StoryNicknameSearchPort {

	Set<UUID> findMemberUuidsByNickname(String nickname);
}
