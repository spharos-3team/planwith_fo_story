package com.planwith.planwith_fo_story;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.planwith.planwith_fo_story.config.AuthProperties;
import com.planwith.planwith_fo_story.config.DeployProperties;

@SpringBootApplication
@EnableConfigurationProperties({AuthProperties.class, DeployProperties.class})
public class PlanwithFoStoryApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlanwithFoStoryApplication.class, args);
	}

}
