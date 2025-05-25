package com.test.translationapp.model;

public class SpeechTranslateResponse {
    private String transcription;
    private String translation;


    public SpeechTranslateResponse() {}

    public SpeechTranslateResponse(String transcription, String translation) {
        this.transcription = transcription;
        this.translation = translation;
    }

    public String getTranscription() {
        return transcription;
    }

    public void setTranscription(String transcription) {
        this.transcription = transcription;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }
}

