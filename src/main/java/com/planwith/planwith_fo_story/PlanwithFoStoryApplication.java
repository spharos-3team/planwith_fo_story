package com.planwith.planwith_fo_story;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.planwith.planwith_fo_story.config.AuthProperties;
import com.planwith.planwith_fo_story.config.DeployProperties;
import com.planwith.planwith_fo_story.config.StoryCacheProperties;
import com.planwith.planwith_fo_story.config.StoryKafkaProperties;
import com.planwith.planwith_fo_story.config.StoryOutboxProperties;
import com.planwith.planwith_fo_story.config.StoryScheduleProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({
		AuthProperties.class,
		DeployProperties.class,
		StoryCacheProperties.class,
		StoryKafkaProperties.class,
		StoryOutboxProperties.class,
		StoryScheduleProperties.class
})
public class PlanwithFoStoryApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlanwithFoStoryApplication.class, args);
	}

}
