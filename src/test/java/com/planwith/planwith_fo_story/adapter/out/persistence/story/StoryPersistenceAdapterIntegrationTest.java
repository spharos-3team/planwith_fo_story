package com.planwith.planwith_fo_story.adapter.out.persistence.story;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_story.application.port.out.StoryCommandPort;
import com.planwith.planwith_fo_story.application.port.out.StoryQueryPort;
import com.planwith.planwith_fo_story.domain.model.AiModerationStatus;
import com.planwith.planwith_fo_story.domain.model.Story;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;
import com.planwith.planwith_fo_story.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_story.domain.model.vo.StoryUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StoryPersistenceAdapterIntegrationTest {

	@Autowired
	private StoryCommandPort storyCommandPort;

	@Autowired
	private StoryQueryPort storyQueryPort;

	@Test
	void savesAndLoadsStoryUsingErdColumns() {
		UUID memberUuid = UUID.randomUUID();
		Story created = Story.create(
				StoryUuid.generate(),
				MemberUuid.of(memberUuid),
				null,
				"여행 기록",
				"내용을 작성합니다.",
				"https://img.example/cover.png",
				"Korea",
				"Busan",
				"해운대",
				LocalDate.of(2026, 8, 1),
				LocalDate.of(2026, 8, 5),
				true,
				VisibilityScope.MEMBER,
				LocalDateTime.of(2026, 8, 23, 10, 0)
		);

		Story saved = storyCommandPort.save(created);
		Story loaded = storyQueryPort.findActiveByStoryUuid(saved.storyUuid().value()).orElseThrow();

		assertThat(loaded.title()).isEqualTo("여행 기록");
		assertThat(loaded.visitCity()).isEqualTo("Busan");
		assertThat(loaded.visibilityScope()).isEqualTo(VisibilityScope.MEMBER);
		assertThat(loaded.aiModerationStatus()).isEqualTo(AiModerationStatus.UNVERIFIED);
		assertThat(loaded.storyLikeCount()).isZero();
		assertThat(loaded.commentEnabled()).isTrue();
		assertThat(loaded.deletedAt()).isNull();
		assertThat(storyQueryPort.findActiveByMemberUuid(memberUuid, 0, 10)).hasSize(1);
	}
}
