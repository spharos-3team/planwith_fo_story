package com.planwith.planwith_fo_story.adapter.out.persistence.projection;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_story.application.port.out.MemberProfileProjectionPort;
import com.planwith.planwith_fo_story.domain.model.projection.MemberProfileProjection;
import com.planwith.planwith_fo_story.domain.model.vo.MemberUuid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberProfileProjectionAdapter implements MemberProfileProjectionPort {

	private final SpringDataStoryMemberProjectionRepository repository;

	@Override
	@Transactional
	public void save(MemberProfileProjection projection) {
		StoryMemberProjectionJpaEntity entity = repository.findById(projection.memberUuid().value())
				.orElseGet(() -> new StoryMemberProjectionJpaEntity(projection.memberUuid().value()));
		if (entity.sourceVersion() > 0 && projection.sourceVersion() < entity.sourceVersion()) {
			log.debug("MemberProfileProjectionAdapter : save : 이전 version Projection 무시 - memberUuid={}",
					projection.memberUuid());
			return;
		}
		entity.apply(
				projection.nickname(),
				projection.profileImage(),
				projection.memberStatus(),
				projection.sourceVersion(),
				projection.synchronizedAt()
		);
		repository.save(entity);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<MemberProfileProjection> findByMemberUuid(UUID memberUuid) {
		return repository.findById(memberUuid)
				.map(entity -> new MemberProfileProjection(
						MemberUuid.of(entity.memberUuid()),
						entity.nickname(),
						entity.profileImage(),
						entity.memberStatus(),
						entity.sourceVersion(),
						entity.synchronizedAt()
				));
	}
}
