package com.planwith.planwith_fo_story.composition.adapter.out.member;

import java.util.Optional;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_story.composition.application.port.out.MemberBioQueryPort;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(
		prefix = "story.detail-screen.member",
		name = "query-enabled",
		havingValue = "false",
		matchIfMissing = true
)
public class DisabledMemberBioQueryAdapter implements MemberBioQueryPort {

	@Override
	public Optional<String> findBioByMemberUuid(UUID memberUuid) {
		log.debug(
				"DisabledMemberBioQueryAdapter : findBioByMemberUuid : Member Service 미연동 상태 - memberUuid={}",
				memberUuid
		);
		return Optional.empty();
	}
}
