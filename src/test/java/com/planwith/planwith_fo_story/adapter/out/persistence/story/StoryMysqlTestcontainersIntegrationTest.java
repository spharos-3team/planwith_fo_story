package com.planwith.planwith_fo_story.adapter.out.persistence.story;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import com.planwith.planwith_fo_story.adapter.out.persistence.outbox.SpringDataStoryOutboxRepository;
import com.planwith.planwith_fo_story.application.command.CreateStoryCommandFactory;
import com.planwith.planwith_fo_story.application.port.in.StoryCommandUseCase;
import com.planwith.planwith_fo_story.application.query.StoryDetailView;
import com.planwith.planwith_fo_story.domain.model.Story;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;
import com.planwith.planwith_fo_story.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_story.domain.model.vo.StoryUuid;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@ActiveProfiles("test")
class StoryMysqlTestcontainersIntegrationTest {

	@Container
	@ServiceConnection
	static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.0.36");

	@Autowired
	private StoryCommandUseCase storyCommandUseCase;

	@Autowired
	private SpringDataStoryRepository storyRepository;

	@Autowired
	private SpringDataStoryOutboxRepository outboxRepository;

	@DynamicPropertySource
	static void disableH2(DynamicPropertyRegistry registry) {
		registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
	}

	@Test
	void persistsStoryAndOutboxOnMysql() {
		StoryDetailView created = storyCommandUseCase.create(CreateStoryCommandFactory.basic(
				UUID.randomUUID(),
				"MySQL 스토리",
				"Testcontainers 본문",
				"https://img.example/cover.png",
				VisibilityScope.ALL
		));

		assertThat(storyRepository.findByStoryUuid(UUID.fromString(created.storyUuid()))).isPresent();
		assertThat(outboxRepository.findAll()).hasSize(1);

		Story restored = Story.create(
				StoryUuid.generate(),
				MemberUuid.of(UUID.randomUUID()),
				null,
				false,
				"추가 저장",
				"본문",
				"https://img.example/cover.png",
				LocalDate.of(2026, 8, 1),
				LocalDate.of(2026, 8, 5),
				true,
				VisibilityScope.PRIVATE,
				LocalDateTime.of(2026, 8, 23, 12, 0)
		);
		assertThat(restored.title()).isEqualTo("추가 저장");
	}
}
