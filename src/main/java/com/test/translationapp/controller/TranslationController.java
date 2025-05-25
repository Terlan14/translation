package com.test.translationapp.controller;


import com.test.translationapp.model.SpeechTranslateResponse;
import com.test.translationapp.service.SpeechRecognitionService;
import com.test.translationapp.service.TranslationService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api")
public class TranslationController {

    private final TranslationService translationService;
    private final SpeechRecognitionService speechRecognitionService;

    public TranslationController(TranslationService translationService, SpeechRecognitionService speechRecognitionService) {
        this.translationService = translationService;
        this.speechRecognitionService = speechRecognitionService;
    }

    @PostMapping(value = "/translate", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String translate(
            @RequestParam String text, @RequestParam String source , @RequestParam String target) {

        try {

            return translationService.translate(text, source, target);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Translation failed "+e.getMessage());
        }
    }


    @PostMapping(value = "/speech-translate",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SpeechTranslateResponse translateSpeech(@RequestParam("file") MultipartFile file,
                                                  @RequestParam("speechSource")String sourceLang,
                                                  @RequestParam("speechTarget") String targetLang) throws Exception {

        try {
            String recognizedText = speechRecognitionService.transcribeAudio(file.getBytes(), sourceLang);
            String translatedText = speechRecognitionService.translateText(recognizedText, targetLang);
            return new SpeechTranslateResponse(recognizedText, translatedText);
        }
        catch (Exception e){
            e.printStackTrace();
            throw  new RuntimeException("Speech translation failed "+e.getMessage());
        }


    }
    @GetMapping("/start-speech")
    public Map<String, String> startSpeech(
            @RequestParam(defaultValue = "en-US") String sourceLang,
            @RequestParam(defaultValue = "az") String targetLang,
            @RequestParam(defaultValue = "15") int durationSeconds) {

        Map<String, String> response = new HashMap<>();

        new Thread(() -> {
            try {
                System.out.println("🎙 Mikrofon işə salındı... (" + durationSeconds + " saniyə)");
                String spokenText = speechRecognitionService.realtimeTranscription(sourceLang, durationSeconds);

                System.out.println("✏️ Danışılan mətn: " + spokenText);
                String translated = speechRecognitionService.translateText(spokenText, targetLang);
                System.out.println("🌐 Tərcümə edilmiş mətn: " + translated);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        response.put("status", "success");
        response.put("message", "Real-time danışıq başladı");
        response.put("sourceLang", sourceLang);
        response.put("targetLang", targetLang);
        return response;
    }


}