package com.forma.api.service;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.forma.api.dto.CreatePostRequest;
import com.forma.api.dto.PostAnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@ConditionalOnProperty(name = "ai.provider", havingValue = "claude")
@RequiredArgsConstructor
public class ClaudeAIService implements PostAnalysisInterface {


    private final AnthropicClient claudClient;
    private final ObjectMapper objectMapper;

    @Override
    public PostAnalysisResponse analysePost(CreatePostRequest request) {
        // Call Claude with post content
        String AI_PROMPT = """
                You are a content moderation assistant tool. I need you to analyse the following post
                including: all text, emoji's, and phrasing - and return a JSON object only. You don't
                need to include any explanation. 
                
                Score Algorithm: 
                Toxicity — profanity, slurs, hate speech, threats, harassment. This should carry the most weight since it's the most clear-cut.
                Misinformation — factual claims that contradict established consensus (science, medicine, history etc). Harder to catch but Claude is quite good at this.
                Manipulative language — fearmongering, clickbait, emotionally charged wording designed to mislead rather than inform.
                Spam signals — repetitive text, nonsensical content, all caps, excessive punctuation.
                
                JSON Structure:
                
                {
                    "score" : <double between 0.0 -> 1.0 where 1.0 means high problematic.>,
                    "reasoning" : <"concise explantion of your score - go into detail">
                }
                
                Post Title: %s
                Post Body: %s """.formatted(request.title(), request.body());

        Message message = claudClient.messages().create(
                MessageCreateParams.builder()
                        .model(Model.CLAUDE_HAIKU_4_5_20251001)
                        .maxTokens(256)
                        .addUserMessage(AI_PROMPT)
                        .build()
        );

        String rawResponse = message.content().stream()
                .filter(ContentBlock::isText)
                .map(block -> block.text().get().text())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No response from Claude"));

        try {
            String cleaned = rawResponse
                    .replaceAll("(?s)```json\\s*", "")
                    .replaceAll("(?s)```\\s*", "")
                    .trim();

            return objectMapper.readValue(cleaned, PostAnalysisResponse.class);

        } catch (Exception e) {
            throw new RuntimeException("Failed to map Claude response: " + rawResponse, e);
        }
    }
}
