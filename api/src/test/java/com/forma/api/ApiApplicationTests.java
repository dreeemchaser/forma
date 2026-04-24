package com.forma.api;

import com.forma.api.dto.CreatePostRequest;
import com.forma.api.dto.PostAnalysisResponse;
import com.forma.api.service.PostAnalysisInterface;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootTest
class ApiApplicationTests {

	@Configuration
	static class TestConfig {
		@Bean
		public PostAnalysisInterface postAnalysisInterface() {
			return request -> new PostAnalysisResponse(0.0, "Test stub");
		}
	}

	@Test
	void contextLoads() {
	}

}
