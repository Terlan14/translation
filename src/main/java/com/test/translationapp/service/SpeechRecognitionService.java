package com.test.translationapp.service;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;

public interface SpeechRecognitionService {
    String transcribeAudio(byte [] audioBytes,String audioFilePath) throws Exception;
    String translateText(String transcribe,String targetLang)throws IOException;

    String realtimeTranscription(String sourceLang, int durationSeconds) throws LineUnavailableException, Exception;
}
