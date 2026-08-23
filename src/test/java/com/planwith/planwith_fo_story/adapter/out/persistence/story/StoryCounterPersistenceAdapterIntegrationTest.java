package com.planwith.planwith_fo_story.adapter.out.persistence.story;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.planwith.planwith_fo_story.application.command.CreateStoryCommandFactory;
import com.planwith.planwith_fo_story.application.port.in.StoryCommandUseCase;
import com.planwith.planwith_fo_story.application.port.in.StoryQueryUseCase;
import com.planwith.planwith_fo_story.application.port.out.StoryCounterPort;
import com.planwith.planwith_fo_story.application.query.GetStoryDetailQuery;
import com.planwith.planwith_fo_story.application.query.StoryDetailView;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;

@SpringBootTest
@ActiveProfiles("test")
class StoryCounterPersistenceAdapterIntegrationTest {

	@Autowired
	private StoryCommandUseCase storyCommandUseCase;

	@Autowired
	private StoryQueryUseCase storyQueryUseCase;

	@Autowired
	private StoryCounterPort storyCounterPort;

	@Autowired
	private SpringDataStoryRepository storyRepository;

	@BeforeEach
	void setUp() {
		storyRepository.deleteAll();
	}

	@Test
	void updatesCountersWithoutLoadingAndSavingTheStoryAggregate() {
		StoryDetailView created = storyCommandUseCase.create(CreateStoryCommandFactory.basic(
				UUID.randomUUID(),
				"Counter story",
				"Counter content",
				"https://img.example/cover.png",
				VisibilityScope.ALL
		));
		UUID storyUuid = UUID.fromString(created.storyUuid());

		assertThat(storyCounterPort.incrementViewCount(storyUuid)).isTrue();
		assertThat(storyCounterPort.changeLikeCount(storyUuid, 1L)).isTrue();
		assertThat(storyCounterPort.changeCommentCount(storyUuid, 1L)).isTrue();
		assertThat(storyCounterPort.changeCommentCount(storyUuid, -2L)).isTrue();

		StoryDetailView detail = storyQueryUseCase.getDetail(new GetStoryDetailQuery(storyUuid, null));
		assertThat(detail.viewCount()).isEqualTo(1L);
		assertThat(detail.storyLikeCount()).isEqualTo(1L);
		assertThat(detail.storyCommentCount()).isZero();
	}
}
