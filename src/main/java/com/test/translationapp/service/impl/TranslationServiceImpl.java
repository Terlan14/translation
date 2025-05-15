package com.test.translationapp.service.impl;

import com.test.translationapp.service.TranslationService;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TranslationServiceImpl implements TranslationService {

    private static final String cloudTranslationKey="AIzaSyD0MbxJC_6wdlAV5Sr4M-A7Hx_3l71vSLs";
    private static final String openApiKey="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";
    private final RestTemplate restTemplate = new RestTemplate();


    @Override
    public String getGoogleTranslation(String text ,String source, String target) {
        String url = "https://translation.googleapis.com/language/translate/v2?key=" + cloudTranslationKey;



        String requestBody = String.format("""
            {
              "q": "%s",
              "source": "%s",
              "target": "%s",
              "format": "text"
            }
            """, text, source, target);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw new RuntimeException("Translation failed: " + response.getStatusCode());
        }


    }


    @Override
    public String openAITranslation(String translatedText) {
        String url = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + openApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Build the chat-style request body
        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-3.5-turbo");

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of(
                "role", "system",
                "content", "You are a helpful assistant that improves translations for clarity and accuracy."
        ));
        messages.add(Map.of(
                "role", "user",
                "content", "Improve the following translation: \"" + translatedText + "\""
        ));

        body.put("messages", messages);
        body.put("max_tokens", 200);
        body.put("temperature", 0.7);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw new RuntimeException("Post-processing failed: " + response.getStatusCode() + "\n" + response.getBody());
        }
    }

    @Override
    public String translate(String text, String source, String target) {
        //return openAITranslation(getGoogleTranslation(text, source, target));
        return getGoogleTranslation(text, source, target);

    }


}
