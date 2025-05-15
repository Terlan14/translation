package com.test.translationapp.service;

public interface TranslationService {
    String getGoogleTranslation(String text,String source, String target);
    String openAITranslation(String translatedText);
    String translate(String text, String source, String target);
}
